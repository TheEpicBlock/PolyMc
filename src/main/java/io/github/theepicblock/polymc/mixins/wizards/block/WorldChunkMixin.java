package io.github.theepicblock.polymc.mixins.wizards.block;

import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.api.block.BlockPoly;
import io.github.theepicblock.polymc.api.wizard.Wizard;
import io.github.theepicblock.polymc.api.misc.PolyMapProvider;
import io.github.theepicblock.polymc.api.wizard.WizardView;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.misc.PolyMapMap;
import io.github.theepicblock.polymc.impl.misc.WatchListener;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.Int2ObjectBiMap;
import net.minecraft.util.collection.PackedIntegerArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
@Mixin(WorldChunk.class)
public abstract class WorldChunkMixin implements WatchListener, WizardView {
	@Shadow @Final private ChunkSection[] sections;
	@Shadow public abstract World getWorld();
	@Shadow public abstract ChunkPos getPos();

	@Shadow @Final private ChunkPos pos;
	@Unique private final PolyMapMap<Map<BlockPos,Wizard>> wizards = new PolyMapMap<>(this::createWizardsForChunk);
	@Unique private final ArrayList<ServerPlayerEntity> players = new ArrayList<>();

	@Unique
	private Map<BlockPos,Wizard> createWizardsForChunk(PolyMap map) {
		Map<BlockPos,Wizard> ret = new HashMap<>();

		for (ChunkSection section : this.sections) {
			if (section == null) continue;

			PalettedContainer<BlockState> container = section.getContainer();
			Palette<BlockState> palette = ((PalettedContainerAccessor<BlockState>)container).getPalette();
			if (palette instanceof ArrayPalette) {
				ret.putAll(createWizardsArrayPalette(map, (ArrayPalette<BlockState>)palette, container, section.getYOffset()));
			} else if (palette instanceof BiMapPalette) {
				ret.putAll(createWizardsBiMapPalette(map, (BiMapPalette<BlockState>)palette, container, section.getYOffset()));
			} else {
				ret.putAll(createWizardsBruteForce(map, palette, container, section.getYOffset()));
			}
			//TODO implementation for Lithium's palette
		}

		return ret;
	}

	@Unique
	private Map<BlockPos,Wizard> createWizardsBiMapPalette(PolyMap polyMap, BiMapPalette<BlockState> palette, PalettedContainer<BlockState> container, int yOffset) {
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

		return createWizards(idToWizMap, container, yOffset);
	}

	@Unique
	private Map<BlockPos,Wizard> createWizardsArrayPalette(PolyMap map, ArrayPalette<BlockState> palette, PalettedContainer<BlockState> container, int yOffset) {
		Int2ObjectMap<BlockPoly> idToWizMap = new Int2ObjectArrayMap<>(5);
		for (int i = 0; i < palette.getSize(); i++) {
			BlockState state = palette.getByIndex(i);
			if (state == null) continue;

			BlockPoly poly = map.getBlockPoly(state.getBlock());
			if (poly != null && poly.hasWizard()) {
				idToWizMap.put(i, poly);
			}
		}

		return createWizards(idToWizMap, container, yOffset);
	}

	/**
	 * @param knownWizards `ids -> polys` of blocks inside this palettedContainer that are known to have wizards. This should only contain polys with wizards, not all polys.
	 */
	private Map<BlockPos,Wizard> createWizards(Int2ObjectMap<BlockPoly> knownWizards, PalettedContainer<BlockState> container, int yOffset) {
		Map<BlockPos,Wizard> ret = new HashMap<>();

		if (knownWizards.size() == 0) {
			return ret;
		}

		PackedIntegerArray data = ((PalettedContainerAccessor<BlockState>)container).getData();
		for (int i = 0; i < data.getSize(); i++) {
			int id = data.get(i);
			BlockPoly poly = knownWizards.get(id);
			if (poly != null) {
				BlockPos pos = Util.fromPalettedContainerIndex(i).add(this.pos.x*16, yOffset, this.pos.z*16);
				ret.put(pos, poly.createWizard(Vec3d.of(pos).add(0.5,0,0.5), Wizard.WizardState.BLOCK));
			}
		}

		return ret;
	}

	private Map<BlockPos,Wizard> createWizardsBruteForce(PolyMap map, Palette<BlockState> palette, PalettedContainer<BlockState> container, int yOffset) {
		Map<BlockPos,Wizard> ret = new HashMap<>();

		PackedIntegerArray data = ((PalettedContainerAccessor<BlockState>)container).getData();
		for (int i = 0; i < data.getSize(); i++) {
			int id = data.get(i);

			BlockState state = palette.getByIndex(id);
			if (state == null) throw new IllegalStateException(String.format("Id exists in data but not in palette. (local)ID: %d CONTAINER: %s DATA: %s PALETTE: %s", id, container, data, palette));

			BlockPoly poly = map.getBlockPoly(state.getBlock());
			if (poly != null && poly.hasWizard()) {
				BlockPos pos = Util.fromPalettedContainerIndex(i).add(this.pos.x*16, yOffset, this.pos.z*16);
				ret.put(pos, poly.createWizard(Vec3d.of(pos).add(0.5,0,0.5), Wizard.WizardState.BLOCK));
			}
		}

		return ret;
	}

	@Override
	public void addPlayer(ServerPlayerEntity playerEntity) {
		PolyMap map = PolyMapProvider.getPolyMap(playerEntity);
		this.wizards.get(map).values().forEach((wizard) -> wizard.addPlayer(playerEntity));
		players.add(playerEntity);
	}

	@Override
	public void removePlayer(ServerPlayerEntity playerEntity) {
		PolyMap map = PolyMapProvider.getPolyMap(playerEntity);
		this.wizards.get(map).values().forEach((wizard) -> wizard.removePlayer(playerEntity));
		players.remove(playerEntity);
	}

	@Override
	public void removeAllPlayers() {
		this.wizards.values().forEach((wizardMap) -> wizardMap.values().forEach(WatchListener::removeAllPlayers));
		this.wizards.clear();
		this.players.clear();
	}

	@Inject(method = "setBlockState", at = @At("TAIL"))
	private void onSet(BlockPos pos, BlockState state, boolean moved, CallbackInfoReturnable<BlockState> cir) {
		wizards.forEach((polyMap, wizardMap) -> {
			Wizard oldWiz = wizardMap.remove(pos);
			if (oldWiz != null) oldWiz.onRemove();

			BlockPoly poly = polyMap.getBlockPoly(state.getBlock());
			if (poly != null && poly.hasWizard()) {
				BlockPos ipos = pos.toImmutable();
				Wizard wiz = poly.createWizard(Vec3d.of(ipos).add(0.5,0,0.5), Wizard.WizardState.BLOCK);
				wizardMap.put(ipos, wiz);
				for (ServerPlayerEntity player : players) {
					wiz.addPlayer(player);
				}
			}
		});
	}

	@Override
	public PolyMapMap<Wizard> getWizards(BlockPos pos) {
		PolyMapMap<Wizard> ret = new PolyMapMap<>(null);
		this.wizards.forEach((polyMap, wizardMap) -> {
			Wizard wizard = wizardMap.get(pos);
			if (wizard != null) ret.put(polyMap, wizard);
		});
		return ret;
	}

	@Override
	public PolyMapMap<Wizard> removeWizards(BlockPos pos, boolean move) {
		PolyMapMap<Wizard> ret = new PolyMapMap<>(null);

		this.wizards.forEach((polyMap, wizardMap) -> {
			Wizard wizard = wizardMap.remove(pos);
			if (wizard != null) {
				if (!move) wizard.onRemove();
				ret.put(polyMap, wizard);
			}
		});
		return ret;
	}
}
