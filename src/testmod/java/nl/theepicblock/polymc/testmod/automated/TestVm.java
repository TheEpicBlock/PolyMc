package nl.theepicblock.polymc.testmod.automated;

import io.github.theepicblock.polymc.impl.generator.asm.ClientClassLoader;
import io.github.theepicblock.polymc.impl.generator.asm.MethodExecutor;
import io.github.theepicblock.polymc.impl.generator.asm.VirtualMachine;
import io.github.theepicblock.polymc.impl.generator.asm.stack.KnownDouble;
import io.github.theepicblock.polymc.impl.generator.asm.stack.KnownFloat;
import io.github.theepicblock.polymc.impl.generator.asm.stack.KnownInteger;
import io.github.theepicblock.polymc.impl.generator.asm.stack.ops.BinaryOp;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.test.GameTest;
import net.minecraft.test.GameTestException;
import net.minecraft.test.TestContext;

import java.io.IOException;

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

    public static void assertReturns(String func, int expected) throws MethodExecutor.VmException {
        var vm = new VirtualMachine(new ClientClassLoader(), new VirtualMachine.VmConfig() {});
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
}
