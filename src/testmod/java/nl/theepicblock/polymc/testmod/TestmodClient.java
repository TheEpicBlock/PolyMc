package nl.theepicblock.polymc.testmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.model.EntityModelLayer;

public class TestmodClient implements ClientModInitializer {
    public static final EntityModelLayer MAIN_MODEL_LAYER = new EntityModelLayer(Testmod.id("test_entity_asm"), "main");
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(Testmod.TEST_ENTITY_ASM, TestAsmEntityRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(MAIN_MODEL_LAYER, TestAsmEntityModel::model);
    }
}
