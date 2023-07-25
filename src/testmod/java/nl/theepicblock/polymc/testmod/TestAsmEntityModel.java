package nl.theepicblock.polymc.testmod;

import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;

public class TestAsmEntityModel extends EntityModel<TestAsmEntity> {
    private final ModelPart main;

    public TestAsmEntityModel(ModelPart root) {
        this.main = root.getChild("main");
    }

    public static TexturedModelData model() {
        var data = new ModelData();
        var part = data.getRoot();

        part.addChild("main", ModelPartBuilder.create()
                    .uv(0, 0)
                    .cuboid(-16.0F, -16.0F, 0.0F, 16.0F, 16.0F, 12.0F),
                ModelTransform.pivot(0.0F, 24.0F, 0.0F));

        return TexturedModelData.of(data, 16, 16);
    }

    @Override
    public void setAngles(TestAsmEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {

    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        this.main.render(matrices, vertices, light, overlay);
    }
}
