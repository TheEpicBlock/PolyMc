package nl.theepicblock.polymc.testmod.automated;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.github.theepicblock.polymc.impl.generator.asm.ClientClassLoader;
import io.github.theepicblock.polymc.impl.generator.asm.MethodExecutor;
import io.github.theepicblock.polymc.impl.generator.asm.VirtualMachine;
import io.github.theepicblock.polymc.impl.generator.asm.stack.*;
import io.github.theepicblock.polymc.impl.generator.asm.stack.ops.BinaryOp;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.test.*;
import net.minecraft.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

@SuppressWarnings("ConstantValue")
public class TestVm implements FabricGameTest {
    /**
     * Plumbing to run {@link #runVmTest(TestContext, Method, VmTest)} on every method annotated with {@link VmTest} in this class
     */
    @CustomTestProvider
    public Collection<TestFunction> vmRunnerTests() {
        var selfClass = TestVm.class;
        return Arrays.stream(selfClass.getDeclaredMethods())
                .map(method -> new Pair<>(method, method.getAnnotation(VmTest.class)))
                .filter(p -> p.getRight() != null)
                .map(pair -> {
                    var method = pair.getLeft();
                    var annotation = pair.getRight();
                    return TestUtil.testBuilder()
                            .name(method.getName())
                            .func((ctx) -> runVmTest(ctx, method, annotation))
                            .build();
                })
                .collect(Collectors.toList());
    }

    private static void runVmTest(TestContext ctx, Method method, VmTest annotation) {
        var logger = LogManager.getLogger("PolyMc/vm/"+method.getName());
        var vm = new VirtualMachine(new ClientClassLoader(), new VirtualMachine.VmConfig() {
            @Override
            public StackEntry onVmError(String method, boolean returnsVoid, MethodExecutor.VmException e) throws MethodExecutor.VmException {
                logger.info("Error executing "+method+": "+e.createFancyErrorMessage());
                return new UnknownValue("Error executing "+method+": "+e.createFancyErrorMessage());
            }
        });
        try {
            vm.addMethodToStack(Type.getInternalName(method.getDeclaringClass()), method.getName(), Type.getMethodDescriptor(method));
            var result = vm.runToCompletion();
            TestUtil.assertEq(result, new KnownInteger(annotation.expected()));
        } catch (MethodExecutor.VmException e) {
            throw new RuntimeException(e);
        }
        ctx.complete();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    public @interface VmTest {
        int expected();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testVmPrettyPrint(TestContext ctx) {
        var directNull = new MethodExecutor.VmException("Test msg1", null);
        assertContains(ctx, directNull, "Test msg1", "Exception should contain its reason");

        var inner = new MethodExecutor.VmException("Test msg2", directNull);
        assertContains(ctx, inner, "Test msg1", "Exception should contain its inner reason");
        assertContains(ctx, inner, "Test msg2", "Exception should contain its reason");

        var innerInner = new MethodExecutor.VmException("Test msg3", inner);
        assertContains(ctx, innerInner, "Test msg1", "Exception should contain its inner inner reason");
        assertContains(ctx, innerInner, "Test msg2", "Exception should contain its inner reason");
        assertContains(ctx, innerInner, "Test msg3", "Exception should contain its reason");

        var other = new IOException("Test msg4");
        var innerOther = new MethodExecutor.VmException("Test msg5", other);
        assertContains(ctx, innerOther, "Test msg4", "Exception should contain its inner reason");
        assertContains(ctx, innerOther, "IOException", "Exception should contain type of inner class if it's not a VmException");

        var otherOther = new IOException("Test msg6", other);
        var innerOtherOther = new MethodExecutor.VmException("Test msg6", otherOther);
        assertContains(ctx, innerOtherOther, "Test msg4", "Exception should recurse into other exception types");

        ctx.complete();
    }

    private static void assertContains(TestContext ctx, MethodExecutor.VmException e, String contains, String msg) {
        ctx.assertTrue(e.createFancyErrorMessage().contains(contains), msg+" ("+e.createFancyErrorMessage()+")");
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testFloatCmpInstructions(TestContext ctx) {
        // Cmpg should have the same behaviour as cmpl, except for NaN's
        for (var op : new BinaryOp.Op[]{BinaryOp.Op.CMPG, BinaryOp.Op.CMPL}) {
            assertCmp(op, 0, 0, 0); // 0 == 0
            assertCmp(op, 0.0d, -0.0d, 0); // 0 == -0
            assertCmp(op, -0.0d, 0.0d, 0); // -0 == 0
            assertCmp(op, 1, 0, 1); // 1 > 0
            assertCmp(op, 0, 1, -1); // 0 < 1
            assertCmp(op, 5, Double.POSITIVE_INFINITY, -1); // 5 < inf
            assertCmp(op, 5, Double.NEGATIVE_INFINITY, 1); // 5 > -inf
            assertCmp(op, Double.POSITIVE_INFINITY, 5, 1); // inf > 5
            assertCmp(op, Double.NEGATIVE_INFINITY, 5, -1); // -inf < 5
        }

        assertCmp(BinaryOp.Op.CMPG, Double.NaN, 0, 1);
        assertCmp(BinaryOp.Op.CMPG, 0, Double.NaN, 1);
        assertCmp(BinaryOp.Op.CMPG, Double.NaN, Double.NaN, 1);
        assertCmp(BinaryOp.Op.CMPL, Double.NaN, 0, -1);
        assertCmp(BinaryOp.Op.CMPL, 0, Float.NaN, -1);
        assertCmp(BinaryOp.Op.CMPL, Double.NaN, Double.NaN, -1);

        ctx.complete();
    }

    private static void assertCmp(BinaryOp.Op op, double a, double b, int expected) {
        var fInstr = new BinaryOp(new KnownFloat((float)a), new KnownFloat((float)b), op, BinaryOp.Type.FLOAT);
        var dInstr = new BinaryOp(new KnownDouble(a), new KnownDouble(b), op, BinaryOp.Type.DOUBLE);
        assertCmpInner(fInstr, op, a, b, expected);
        assertCmpInner(dInstr, op, a, b, expected);
    }

    private static void assertCmpInner(BinaryOp instruction, BinaryOp.Op op, double a, double b, int expected) {
        try {
            if (!instruction.canBeSimplified()) {
                throw new GameTestException("("+a+" "+op+" "+b+" ="+expected+") instruction should be able to be simplified, considering its inputs are concrete");
            }
            var simple = instruction.simplify(null);
            if (!simple.isConcrete()) {
                throw new GameTestException("("+a+" "+op+" "+b+" ="+expected+") expected result of simplification to be concrete, considering its inputs are concrete");
            }
            var result = simple.extractAs(int.class);
            if (expected != result) {
                throw new GameTestException("Expected "+a+" "+op+" "+b+" = "+expected+", but found "+ result);
            }
        } catch (MethodExecutor.VmException e) {
            throw new GameTestException(e.createFancyErrorMessage());
        }
    }

    @VmTest(expected = 1)
    public static int createOne() {
        return 1;
    }

    @VmTest(expected = 9)
    public static int ifStatement() {
        int integer = 634;
        if (integer + 1 == 635) {
            return 9;
        } else {
            return 3;
        }
    }

    @VmTest(expected = 9)
    public static int invokeStatic() {
        return createOne() + 8;
    }

    @VmTest(expected = 6)
    public static int stringLength() {
        return "abcdef".length();
    }

    @VmTest(expected = 7)
    public static int checkCast() {
        var myGetter = new IntProvider() {
            @Override
            public int gimme() {
                return 7;
            }
        };
        var castGetter = (IntProvider)myGetter;
        return castGetter.gimme();
    }

    private interface IntProvider {
        int gimme();
    }

    @VmTest(expected = 546)
    public static int interfaceDefault() {
        var myRecord = new RecordImplementingDefaultInterface();
        return myRecord.gimme();
    }

    private record RecordImplementingDefaultInterface() implements InterfaceWithDefault{}

    private interface InterfaceWithDefault {
        default int gimme() {
            return 546;
        }
    }

    @VmTest(expected = 35)
    public static int streamCollectMap() {
        var myList = Lists.newArrayList(0, 1, 2, 3, 4);
        int x = 5;
        var newMap = myList.stream().collect(Collectors.toMap(num -> num, num -> num+x));
        return newMap.values().stream().reduce(Integer::sum).orElse(-1);
    }

    @VmTest(expected = 64*2)
    public static int streamCollectImmutableList() {
        var myList = Lists.newArrayList(1, 1, 64, 1, 1);
        var newList = myList
                .stream()
                .map(i -> i+i)
                .collect(ImmutableList.toImmutableList());
        myList.set(2, 999);
        return newList.get(2);
    }

    @VmTest(expected = 1)
    public static int objHashcode() {
        var obj = new Object();
        var obj2 = new Object();
        if (obj.hashCode() != obj2.hashCode()) {
            return 1;
        } else {
            return -1;
        }
    }

    @VmTest(expected = 1)
    public static int classHashcode() {
        var class1 = Object.class;
        var class2 = Integer.class;
        if (class1.hashCode() != class2.hashCode()) {
            return 1;
        } else {
            return -1;
        }
    }

    @VmTest(expected = 1+2+3)
    public static int arrayClone() {
        var array = new int[]{1,2,3};
        var newArray = array.clone();
        array[1] = 5;
        return newArray[0]+newArray[1]+newArray[2];
    }

    @VmTest(expected = 42+65)
    public static int staticFieldInheritance() {
        return StaticB.MyNumA + StaticB.MyNumB;
    }

    @VmTest(expected = 12+19)
    public static int staticMethodInheritance() {
        return StaticB.methA() + StaticB.methB();
    }

    public static class StaticA {
        public static int MyNumA = 42;
        public static int MyNumB = 43;

        public static int methA() {
            return 12;
        }

        public static int methB() {
            return 217;
        }
    }

    public static class StaticB extends StaticA {
        public static int MyNumB = 65;

        public static int methB() {
            return 19;
        }
    }

    @VmTest(expected = 5)
    public static int intToString() {
        return Integer.toString(32576).length();
    }

    @VmTest(expected = 3)
    public static int newArray() {
        var myLength = createOne() + 2; // Just to spice things up a bit
        var myArray = new Object[myLength];
        return myArray.length;
    }
}
