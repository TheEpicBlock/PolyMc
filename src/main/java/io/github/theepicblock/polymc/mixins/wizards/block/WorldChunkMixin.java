package io.github.theepicblock.polymc.mixins.wizards.block;

import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.api.block.BlockPoly;
import io.github.theepicblock.polymc.api.misc.PolyMapProvider;
import io.github.theepicblock.polymc.api.wizard.Wizard;
import io.github.theepicblock.polymc.api.wizard.WizardView;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.misc.PolyMapMap;
import io.github.theepicblock.polymc.impl.misc.WatchListener;
import io.github.theepicblock.polymc.impl.mixin.WizardTickerDuck;
import io.github.theepicblock.polymc.impl.poly.wizard.CachedPolyMapFilteredPlayerView;
import io.github.theepicblock.polymc.impl.poly.wizard.PlacedWizardInfo;
import io.github.theepicblock.polymc.impl.poly.wizard.PolyMapFilteredPlayerView;
import io.github.theepicblock.polymc.impl.poly.wizard.SinglePlayerView;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.PackedIntegerArray;
import net.minecraft.util.collection.PaletteStorage;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.*;
import net.minecraft.world.gen.chunk.BlendingData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
@Mixin(WorldChunk.class)
public abstract class WorldChunkMixin extends Chunk implements WatchListener, WizardView {
    @Unique
    private final PolyMapMap<@NotNull Map<@NotNull BlockPos,@NotNull Wizard>> wizards = new PolyMapMap<>(this::createWizardsForChunk);

    @Shadow @Final World world;

    public WorldChunkMixin(ChunkPos pos, UpgradeData upgradeData, HeightLimitView heightLimitView, Registry<Biome> biome, long inhabitedTime, @Nullable ChunkSection[] sectionArrayInitializer, @Nullable BlendingData blendingData) {
        super(pos, upgradeData, heightLimitView, biome, inhabitedTime, sectionArrayInitializer, blendingData);
    }

    @Shadow public abstract World getWorld();

    @Unique
    private Map<BlockPos,Wizard> createWizardsForChunk(PolyMap map) {
        Map<BlockPos,Wizard> ret = new HashMap<>();
        if (!(this.world instanceof ServerWorld))
            return ret; //Wizards are only passed ServerWorlds, so we can't create any wizards here.
        if (!map.hasBlockWizards())
            return ret;

        for (ChunkSection section : this.sectionArray) {
            if (section == null) continue;

            PalettedContainer<BlockState> container = section.getBlockStateContainer();
            var data = ((PalettedContainerAccessor<BlockState>)container).getData();
            var palette = data.palette();
            var paletteData = data.storage();
            processWizards(map, palette, paletteData, section.getYOffset(), ret);
        }

        return ret;
    }

    @Unique
    private void processWizards(PolyMap polyMap, Palette<BlockState> palette, PaletteStorage data, int yOffset, Map<@NotNull BlockPos,@NotNull Wizard> wizardMap) {
        if (data.getSize() == 0) return;

        if (palette.getSize() < 256) {
            // The palette contains all block states present in the chunk
            var idsWithPolys = new BlockPoly[palette.getSize()];
            for (int i = 0; i < palette.getSize(); i++) {
                var state = palette.get(i);
                var poly = polyMap.getBlockPoly(state.getBlock());
                if (poly != null && poly.hasWizard()) {
                    idsWithPolys[i] = poly;
                }
            }

            if (data instanceof PackedIntegerArray) {
                // Fast way of iterating the packed data with an index
                int i = 0;

                var elementBits = data.getElementBits();
                var elementsPerLong = (char)(64 / elementBits);
                var maxValue = (1L << elementBits) - 1L;
                var size = data.getSize();

                data:
                for (long l : data.getData()) {
                    for (int j = 0; j < elementsPerLong; ++j) {
                        var blockIndex = (int)(l & maxValue);
                        var poly = idsWithPolys[blockIndex];
                        if (poly != null) {
                            processBlock(polyMap, poly, i, yOffset, wizardMap);
                        }

                        l >>= elementBits;
                        ++i;
                        if (i >= size) {
                            break data;
                        }
                    }
                }
            } else {
                for (int i = 0; i < data.getSize(); i++) {
                    var blockIndex = data.get(i);
                    var poly = idsWithPolys[blockIndex];
                    if (poly != null) {
                        processBlock(polyMap, poly, i, yOffset, wizardMap);
                    }
                }
            }
        } else {
            // It's not worth iterating the palette, instead iterate the blocks in the data
            if (data instanceof PackedIntegerArray) {
                // Fast way of iterating the packed data with an index
                int i = 0;

                var elementBits = data.getElementBits();
                var elementsPerLong = (char)(64 / elementBits);
                var maxValue = (1L << elementBits) - 1L;
                var size = data.getSize();

                data:
                for (long l : data.getData()) {
                    for (int j = 0; j < elementsPerLong; ++j) {
                        var blockIndex = (int)(l & maxValue);
                        var poly = polyMap.getBlockPoly(palette.get(blockIndex).getBlock());
                        if (poly != null && poly.hasWizard()) {
                            processBlock(polyMap, poly, i, yOffset, wizardMap);
                        }

                        l >>= elementBits;
                        ++i;
                        if (i >= size) {
                            break data;
                        }
                    }
                }
            } else {
                for (int i = 0; i < data.getSize(); i++) {
                    var blockIndex = data.get(i);
                    var poly = polyMap.getBlockPoly(palette.get(blockIndex).getBlock());
                    if (poly != null && poly.hasWizard()) {
                        processBlock(polyMap, poly, i, yOffset, wizardMap);
                    }
                }
            }
        }
    }

    @Unique
    private void processBlock(@NotNull PolyMap map, @NotNull BlockPoly poly, int index, int yOffset, @NotNull Map<@NotNull BlockPos,@NotNull Wizard> wizardMap) {
        BlockPos pos = Util.fromPalettedContainerIndex(index).add(this.pos.x * 16, yOffset, this.pos.z * 16);
        try {
            var wiz = poly.createWizard(new PlacedWizardInfo(pos, (ServerWorld)this.world));
            if (wiz == null) {
                PolyMc.LOGGER.warn(poly+" is creating null wizards! This is bad!");
                return;
            }
            ((WizardTickerDuck)this.world).polymc$addBlockTicker(map, this.getPos(), wiz);
            wizardMap.put(pos, wiz);
        } catch (Throwable t) {
            PolyMc.LOGGER.warn("Failed to create block wizard for block at "+pos+" | "+poly);
        }
    }

    @Override
    public void polymc$addPlayer(ServerPlayerEntity playerEntity) {
        PolyMap map = PolyMapProvider.getPolyMap(playerEntity);
        this.wizards.get(map).values().forEach((wizard) -> {
            try {
                var playerView = new SinglePlayerView(playerEntity);
                wizard.addPlayer(playerView);
                playerView.sendBatched();
            } catch (Throwable t) {
                PolyMc.LOGGER.error("Failed to add player to wizard "+wizard);
            }
        });
    }

    @Override
    public void polymc$removePlayer(ServerPlayerEntity playerEntity) {
        PolyMap map = PolyMapProvider.getPolyMap(playerEntity);
        this.wizards.get(map).values().forEach((wizard) -> {
            try {
                var playerView = new SinglePlayerView(playerEntity);
                wizard.removePlayer(playerView);
                playerView.sendBatched();
            } catch (Throwable t) {
                PolyMc.LOGGER.error("Failed to remove player from wizard "+wizard);
            }
        });
    }

    @Override
    public void polymc$removeAllPlayers() {
        var allPlayers = PolyMapFilteredPlayerView.getAll((ServerWorld)world, this.getPos());
        this.wizards.forEach((polyMap, wizardMap) -> {
            if (!wizardMap.isEmpty()) {
                var players = new CachedPolyMapFilteredPlayerView(allPlayers, polyMap); // The players nearby this chunk, for packet purposes
                wizardMap.values().forEach(wizard -> {
                    try {
                        wizard.removeAllPlayers(players);
                    } catch (Throwable t) {
                        PolyMc.LOGGER.error("Failed to remove all players from wizard " + wizard);
                    }
                    ((WizardTickerDuck)this.world).polymc$removeBlockTicker(polyMap, this.getPos(), wizard);
                });
                players.sendBatched();
            }
        });
        this.wizards.clear();
    }

    @Inject(method = "setBlockState", at = @At("TAIL"))
    private void onSet(BlockPos pos, BlockState state, boolean moved, CallbackInfoReturnable<BlockState> cir) {
        List<ServerPlayerEntity> allPlayers = null;
        for (var entry : wizards.entrySet()) {
            var polyMap = entry.getKey();
            var wizardMap = entry.getValue();

            Wizard oldWiz = wizardMap.remove(pos);
            if (oldWiz != null) {
                if (allPlayers == null) {
                    allPlayers = PolyMapFilteredPlayerView.getAll((ServerWorld)this.getWorld(), this.getPos());
                }
                var view = new PolyMapFilteredPlayerView(allPlayers, polyMap);
                oldWiz.onRemove(view);
                view.sendBatched();
                ((WizardTickerDuck)this.world).polymc$removeBlockTicker(polyMap, this.getPos(), oldWiz);
            }

            BlockPoly poly = polyMap.getBlockPoly(state.getBlock());
            if (poly != null && poly.hasWizard()) {
                try {
                    BlockPos ipos = pos.toImmutable();
                    Wizard wiz = poly.createWizard(new PlacedWizardInfo(ipos, (ServerWorld)this.world));
                    wizardMap.put(ipos, wiz);
                    if (allPlayers == null) {
                        allPlayers = PolyMapFilteredPlayerView.getAll((ServerWorld)this.getWorld(), this.getPos());
                    }

                    var filteredView = new PolyMapFilteredPlayerView(allPlayers, polyMap);
                    wiz.addPlayer(filteredView);
                    ((WizardTickerDuck)this.world).polymc$addBlockTicker(polyMap, this.getPos(), wiz);
                } catch (Throwable t) {
                    PolyMc.LOGGER.error("Failed to create block wizard for " + state.getBlock().getTranslationKey() + " | " + poly);
                    t.printStackTrace();
                }
            }
        }
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
        var allPlayers = move ? null : PolyMapFilteredPlayerView.getAll((ServerWorld)this.getWorld(), this.getPos());

        this.wizards.forEach((polyMap, wizardMap) -> {
            Wizard wizard = wizardMap.remove(pos);
            if (wizard != null) {
                try {
                    if (!move) {
                        var view = new PolyMapFilteredPlayerView(allPlayers, polyMap);
                        wizard.onRemove(view);
                        view.sendBatched();
                    }
                } catch (Throwable t) {
                    PolyMc.LOGGER.error("Failed to remove wizard "+wizard);
                    t.printStackTrace();
                }
                ((WizardTickerDuck)this.world).polymc$removeBlockTicker(polyMap, this.getPos(), wizard);
                ret.put(polyMap, wizard);
            }
        });
        return ret;
    }
}
