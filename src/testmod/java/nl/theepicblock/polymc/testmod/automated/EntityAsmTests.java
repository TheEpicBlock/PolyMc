package nl.theepicblock.polymc.testmod.automated;

import io.github.theepicblock.polymc.api.PolyRegistry;
import io.github.theepicblock.polymc.impl.generator.asm.ClientInitializerAnalyzer;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import nl.theepicblock.polymc.testmod.Testmod;

public class EntityAsmTests implements FabricGameTest {
    public static final PolyRegistry registry = new PolyRegistry();

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void clientInitializerAnalyzer(TestContext ctx) {
        var initAnalyzer = registry.getSharedValues(ClientInitializerAnalyzer.KEY);

        var renderer = initAnalyzer.getEntityRenderer(Testmod.TEST_ENTITY_ASM);
        TestUtil.assertEq(renderer.method().getOwner(), "nl/theepicblock/polymc/testmod/TestAsmEntityRenderer");

        // Should equal TestmodClient#MAIN_MODEL_LAYER
        var modelLayerId = new ClientInitializerAnalyzer.EntityModelLayer(Testmod.id("test_entity_asm"), "main");
        var modelLayer = initAnalyzer.getEntityModelLayer(modelLayerId);
        TestUtil.assertEq(modelLayer.method().getOwner(), "nl/theepicblock/polymc/testmod/TestAsmEntityModel");

        ctx.complete();
    }
}
