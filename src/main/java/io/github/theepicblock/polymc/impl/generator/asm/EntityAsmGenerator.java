package io.github.theepicblock.polymc.impl.generator.asm;

import org.objectweb.asm.ClassReader;

import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.impl.misc.InternalEntityHelpers;
import io.github.theepicblock.polymc.impl.poly.entity.EntityWizard;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class EntityAsmGenerator {
    public static <E extends Entity> EntityWizard<E> create(EntityRendererFactory<E> source, Identifier id, EntityType<E> type) {
        var ctx = new EntityRendererFactory.Context(
            null,
            null,
            null,
            null,
            null,
            null,
            null);
        var renderer = source.create(ctx);
        if (renderer == null) throw new NullPointerException("Failed to create renderer for "+id);
        var rendererJavaClass = renderer.getClass();

        var rendererClass = new ClassReader(null, 0, 0); // TODO

        rendererClass.accept(null, 0);

        PolyMc.LOGGER.info("Texture of "+id+" is "+renderer.getTexture(InternalEntityHelpers.getEntity(type)));

        return null;
    }
}
