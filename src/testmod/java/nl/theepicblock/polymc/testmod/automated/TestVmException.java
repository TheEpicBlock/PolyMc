package nl.theepicblock.polymc.testmod.automated;

import io.github.theepicblock.polymc.impl.generator.asm.MethodExecutor;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

import java.io.IOException;

public class TestVmException implements FabricGameTest {
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testPrettyPrint(TestContext ctx) {
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
}
