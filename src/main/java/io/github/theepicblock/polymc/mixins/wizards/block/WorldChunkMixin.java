package io.github.theepicblock.polymc.mixins.wizards.block;

import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.api.block.BlockPoly;
import io.github.theepicblock.polymc.api.block.BlockWizard;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.misc.PolyMapMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.collection.PackedIntegerArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
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
			}
		}

		return ret;
	}

	@Unique
	private Map<BlockPos, BlockWizard> createWizardsArrayPalette(PolyMap map, ArrayPalette<BlockState> palette, PalettedContainer<BlockState> container) {
		IntArrayList wizardBlockStates = new IntArrayList();
		for (int i = 0; i < palette.getSize(); i++) {
			BlockState state = palette.getByIndex(i);
			if (state == null) continue;

			BlockPoly poly = map.getBlockPoly(state.getBlock());
			if (poly != null && poly.hasWizard()) {
				wizardBlockStates.add(i);
			}
		}

		return createWizardsByList(map, wizardBlockStates, container);
	}

	/**
	 * @param knownWizards Integer ids inside this palettedContainer that are known to have wizards.
	 */
	private Map<BlockPos, BlockWizard> createWizardsByList(PolyMap map, IntArrayList knownWizards, PalettedContainer<BlockState> container) {
		Map<BlockPos, BlockWizard> ret = new HashMap<>();

		if (knownWizards.size() == 0) {
			return ret;
		}

		PackedIntegerArray data = ((PalettedContainerAccessor<BlockState>)container).getData();
		for (int i = 0; i < data.getSize(); i++) {
			int id = data.get(i);
			if (knownWizards.contains(id)) {
				BlockState state = ((PalettedContainerAccessor<BlockState>)container).getPalette().getByIndex(id);
				if (state == null) throw new IllegalStateException();
				ret.put(Util.fromPalettedContainerIndex(i), map.getBlockPoly(state.getBlock()).getWizard());
			}
		}

		return ret;
	}
}
