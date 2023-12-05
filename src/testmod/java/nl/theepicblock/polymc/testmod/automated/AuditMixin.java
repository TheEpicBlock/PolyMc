package nl.theepicblock.polymc.testmod.automated;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import org.spongepowered.asm.mixin.MixinEnvironment;

public class AuditMixin implements FabricGameTest {
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testMixin(TestContext ctx) {
        MixinEnvironment.getCurrentEnvironment().audit();
        ctx.complete();
    }
}
