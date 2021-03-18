package io.github.theepicblock.polymc.mixins.wizards.block;

import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.api.block.BlockPoly;
import io.github.theepicblock.polymc.api.block.BlockWizard;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.misc.PolyMapMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.block.BlockState;
import net.minecraft.util.collection.Int2ObjectBiMap;
import net.minecraft.util.collection.PackedIntegerArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
@Mixin(WorldChunk.class)
public class WorldChunkMixin {
	@Shadow @Final private ChunkSection[] sections;
	@Unique private final PolyMapMap<Map<BlockPos,BlockWizard>> wizards = new PolyMapMap<>(this::createWizardsForMap);

	@Unique
	private Map<BlockPos, BlockWizard> createWizardsForMap(PolyMap map) {
		Map<BlockPos, BlockWizard> ret = new HashMap<>();

		for (ChunkSection section : this.sections) {
			PalettedContainer<BlockState> container = section.getContainer();
			Palette<BlockState> palette = ((PalettedContainerAccessor<BlockState>)container).getPalette();
			if (palette instanceof ArrayPalette) {
				ret.putAll(createWizardsArrayPalette(map, (ArrayPalette<BlockState>)palette, container));
			} else if (palette instanceof BiMapPalette) {
				ret.putAll(createWizardsBiMapPalette(map, (BiMapPalette<BlockState>)palette, container));
			}
		}

		return ret;
	}

	@Unique
	private Map<BlockPos, BlockWizard> createWizardsBiMapPalette(PolyMap polyMap, BiMapPalette<BlockState> palette, PalettedContainer<BlockState> container) {
		Int2ObjectMap<BlockPoly> idToWizMap = new Int2ObjectArrayMap<>(5);
		Int2ObjectBiMap<BlockState> map = ((BiMapPaletteAccessor<BlockState>)palette).getMap();

		int i = 0;
		for (BlockState state : map) {
			BlockPoly poly = polyMap.getBlockPoly(state.getBlock());
			if (poly != null && poly.hasWizard()) {
				idToWizMap.put(i, poly);
			}
			i++;
		}

		return createWizards(idToWizMap, container);
	}

	@Unique
	private Map<BlockPos, BlockWizard> createWizardsArrayPalette(PolyMap map, ArrayPalette<BlockState> palette, PalettedContainer<BlockState> container) {
		Int2ObjectMap<BlockPoly> idToWizMap = new Int2ObjectArrayMap<>(5);
		for (int i = 0; i < palette.getSize(); i++) {
			BlockState state = palette.getByIndex(i);
			if (state == null) continue;

			BlockPoly poly = map.getBlockPoly(state.getBlock());
			if (poly != null && poly.hasWizard()) {
				idToWizMap.put(i, poly);
			}
		}

		return createWizards(idToWizMap, container);
	}

	/**
	 * @param knownWizards Integer ids inside this palettedContainer that are known to have wizards.
	 */
	private Map<BlockPos, BlockWizard> createWizards(Int2ObjectMap<BlockPoly> knownWizards, PalettedContainer<BlockState> container) {
		Map<BlockPos, BlockWizard> ret = new HashMap<>();

		if (knownWizards.size() == 0) {
			return ret;
		}

		PackedIntegerArray data = ((PalettedContainerAccessor<BlockState>)container).getData();
		for (int i = 0; i < data.getSize(); i++) {
			int id = data.get(i);
			BlockPoly wiz = knownWizards.get(id);
			if (wiz != null) {
				ret.put(Util.fromPalettedContainerIndex(i), wiz.getWizard());
			}
		}

		return ret;
	}
}
