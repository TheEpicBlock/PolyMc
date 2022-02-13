package io.github.theepicblock.polymc.mixins.block;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.util.collection.IdList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

/**
 * Used to remap block ids
 * @see io.github.theepicblock.polymc.impl.misc.BlockIdRemapper
 */
@Mixin(IdList.class)
public interface IdListAccessor<T> {
    @Accessor
    List<T> getList();

    @Accessor
    Object2IntMap<T> getIdMap();
}
