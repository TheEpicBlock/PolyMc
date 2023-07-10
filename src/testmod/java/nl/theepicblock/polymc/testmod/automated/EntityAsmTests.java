package nl.theepicblock.polymc.testmod.automated;

import io.github.theepicblock.polymc.api.PolyRegistry;
import io.github.theepicblock.polymc.impl.generator.asm.ClientInitializerAnalyzer;
import io.github.theepicblock.polymc.impl.generator.asm.EntityRendererAnalyzer;
import io.github.theepicblock.polymc.impl.generator.asm.MethodExecutor;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import nl.theepicblock.polymc.testmod.Testmod;

public class EntityAsmTests implements FabricGameTest {
    public static final PolyRegistry registry = new PolyRegistry();

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void test(TestContext ctx) throws MethodExecutor.VmException {
        var initAnalyzer = registry.getSharedValues(ClientInitializerAnalyzer.KEY);

        var renderer = initAnalyzer.getEntityRenderer(Testmod.TEST_ENTITY_ASM);
        TestUtil.assertEq(renderer.method().getOwner(), "nl/theepicblock/polymc/testmod/TestAsmEntityRenderer");

        // Should equal TestmodClient#MAIN_MODEL_LAYER
        var modelLayerId = new ClientInitializerAnalyzer.EntityModelLayer(Testmod.id("test_entity_asm"), "main");
        var modelLayer = initAnalyzer.getEntityModelLayer(modelLayerId);
        TestUtil.assertEq(modelLayer.method().getOwner(), "nl/theepicblock/polymc/testmod/TestAsmEntityModel");

        // rendering analyzer
        var analysisResults = registry.getSharedValues(EntityRendererAnalyzer.KEY).analyze(Testmod.TEST_ENTITY_ASM);
        TestUtil.assertEq(analysisResults.calls().size(), 1, "There should be one cuboid render call because the test entity only contains one cube");

        ctx.complete();
    }
}
