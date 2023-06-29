package nl.theepicblock.polymc.testmod;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

public class TestAsmEntityRenderer extends MobEntityRenderer<TestAsmEntity, TestAsmEntityModel> {
    public static Identifier TEXTURE = Testmod.id("textures/entity/asm_entity.png");
    public TestAsmEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new TestAsmEntityModel(context.getPart(TestmodClient.MAIN_MODEL_LAYER)), .5f);
    }

    @Override
    public Identifier getTexture(TestAsmEntity entity) {
        return TEXTURE;
    }
}
