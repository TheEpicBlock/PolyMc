package nl.theepicblock.polymc.testmod.automated;

import com.google.common.collect.Lists;
import io.github.theepicblock.polymc.impl.generator.asm.ClientClassLoader;
import io.github.theepicblock.polymc.impl.generator.asm.MethodExecutor;
import io.github.theepicblock.polymc.impl.generator.asm.VirtualMachine;
import io.github.theepicblock.polymc.impl.generator.asm.stack.*;
import io.github.theepicblock.polymc.impl.generator.asm.stack.ops.BinaryOp;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.test.GameTest;
import net.minecraft.test.GameTestException;
import net.minecraft.test.TestContext;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.util.stream.Collectors;

public class TestVm implements FabricGameTest {
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
            var result = simple.extractAs(Integer.class);
            if (expected != result) {
                throw new GameTestException("Expected "+a+" "+op+" "+b+" = "+expected+", but found "+ result);
            }
        } catch (MethodExecutor.VmException e) {
            throw new GameTestException(e.createFancyErrorMessage());
        }
    }

    /**
     * Tests the creation of the local variable array when a method is invoked.
     */
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testLocalVariableArrayCreation(TestContext ctx) {
        var objA = StackEntry.known(new Object());
        var objB = StackEntry.known(new Object());
        var objC = StackEntry.known(new Object());
        var objWide = StackEntry.known(0.0d); // Doubles (and longs) take up two slots

        var stack1 = new ObjectArrayList<StackEntry>();
        stack1.push(objA);
        stack1.push(objB);
        var desc1 = "(Ljava/lang/Object;)V";
        var lva1 = MethodExecutor.assembleLocalVariableArray(Type.getType(desc1), stack1, false);
        TestUtil.assertEq(lva1.length, 2);
        TestUtil.assertEq(lva1[0], objA);
        TestUtil.assertEq(lva1[1], objB);

        var stack2 = new ObjectArrayList<StackEntry>();
        stack2.push(objA);
        stack2.push(objB);
        stack2.push(objC);
        var desc2 = "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;";
        var lva2 = MethodExecutor.assembleLocalVariableArray(Type.getType(desc2), stack2, true);
        TestUtil.assertEq(lva2.length, 3);
        TestUtil.assertEq(lva2[0], objA);
        TestUtil.assertEq(lva2[1], objB);
        TestUtil.assertEq(lva2[2], objC);

        var stack3 = new ObjectArrayList<StackEntry>();
        stack3.push(objA);
        stack3.push(objWide);
        var desc3 = "(D)V";
        var lva3 = MethodExecutor.assembleLocalVariableArray(Type.getType(desc3), stack3, false);
        TestUtil.assertEq(lva3.length, 3);
        TestUtil.assertEq(lva3[0], objA);
        TestUtil.assertEq(lva3[1], objWide);
        TestUtil.assertEq(lva3[2], null, "The second slot of a wide object should be unused");

        var stack4 = new ObjectArrayList<StackEntry>();
        stack4.push(objWide);
        var desc4 = "(D)V";
        var lva4 = MethodExecutor.assembleLocalVariableArray(Type.getType(desc4), stack4, true);
        TestUtil.assertEq(lva4.length, 2);
        TestUtil.assertEq(lva4[0], objWide);
        TestUtil.assertEq(lva4[1], null, "The second slot of a wide object should be unused");

        ctx.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void createOne(TestContext ctx) throws MethodExecutor.VmException {
        // A function that just returns 1
        assertReturns("createOneFunc", 1);
        ctx.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void invoke(TestContext ctx) throws MethodExecutor.VmException {
        // Function that invokes another function
        assertReturns("invokeFunc", 9);
        ctx.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void ifStatement(TestContext ctx) throws MethodExecutor.VmException {
        // Function with an if statement
        assertReturns("ifStatementFunc", 9);
        ctx.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void stringLength(TestContext ctx) throws MethodExecutor.VmException {
        assertReturns("stringLengthFunc", 6);
        ctx.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void checkCast(TestContext ctx) throws MethodExecutor.VmException {
        assertReturns("checkCastFunc", 7);
        ctx.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void interfaceDefault(TestContext ctx) throws MethodExecutor.VmException {
        assertReturns("interfaceDefaultFunc", 546);
        ctx.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void streamCollect(TestContext ctx) throws MethodExecutor.VmException {
        assertReturns("streamCollectFunc", 35);
        ctx.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void objHashcode(TestContext ctx) throws MethodExecutor.VmException {
        assertReturns("objHashcodeFunc", 1);
        ctx.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void classHashcode(TestContext ctx) throws MethodExecutor.VmException {
        assertReturns("classHashcodeFunc", 1);
        ctx.complete();
    }

    public static void assertReturns(String func, int expected) throws MethodExecutor.VmException {
        var vm = new VirtualMachine(new ClientClassLoader(), new VirtualMachine.VmConfig() {
            @Override
            public StackEntry onVmError(String method, boolean returnsVoid, MethodExecutor.VmException e) throws MethodExecutor.VmException {
                return new UnknownValue("");
            }
        });
        vm.addMethodToStack(TestVm.class.getName().replace(".", "/"), func, "()I"); // All test functions have a descriptor of ()I
        var result = vm.runToCompletion();
        TestUtil.assertEq(result, new KnownInteger(expected));

    }

    public static int ifStatementFunc() {
        int integer = 634;
        if (integer + 1 == 635) {
            return 9;
        } else {
            return 3;
        }
    }

    public static int invokeFunc() {
        return createOneFunc() + 8;
    }

    public static int createOneFunc() {
        return 1;
    }

    public static int stringLengthFunc() {
        return "abcdef".length();
    }

    public static int checkCastFunc() {
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

    public static int interfaceDefaultFunc() {
        var myRecord = new RecordImplementingDefaultInterface();
        return myRecord.gimme();
    }

    private record RecordImplementingDefaultInterface() implements InterfaceWithDefault{}

    private interface InterfaceWithDefault {
        default int gimme() {
            return 546;
        }
    }

    public static int streamCollectFunc() {
        var myList = Lists.newArrayList(0, 1, 2, 3, 4);
        int x = 5;
        var newMap = myList.stream().collect(Collectors.toMap(num -> num, num -> num+x));
        return newMap.values().stream().reduce(Integer::sum).orElse(-1);
    }

    public static int objHashcodeFunc() {
        var obj = new Object();
        var obj2 = new Object();
        if (obj.hashCode() != obj2.hashCode()) {
            return 1;
        } else {
            return -1;
        }
    }

    public static int classHashcodeFunc() {
        var class1 = Object.class;
        var class2 = Integer.class;
        if (class1.hashCode() != class2.hashCode()) {
            return 1;
        } else {
            return -1;
        }
    }
}
