package io.github.theepicblock.polymc.impl;

import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.api.block.BlockPoly;
import io.github.theepicblock.polymc.api.entity.EntityPoly;
import io.github.theepicblock.polymc.api.gui.GuiPoly;
import io.github.theepicblock.polymc.api.item.ItemLocation;
import io.github.theepicblock.polymc.api.item.ItemPoly;
import io.github.theepicblock.polymc.api.resource.PolyMcResourcePack;
import io.github.theepicblock.polymc.impl.misc.logging.SimpleLogger;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class NOPPolyMap implements PolyMap {
    @Override
    public ItemStack getClientItem(ItemStack serverItem, @Nullable ServerPlayerEntity player, @Nullable ItemLocation location) {
        return serverItem;
    }

    @Override
    public BlockState getClientState(BlockState serverBlock, @Nullable ServerPlayerEntity player) {
        return serverBlock;
    }

    @Override
    public ItemPoly getItemPoly(Item item) {
        return null;
    }

    @Override
    public GuiPoly getGuiPoly(ScreenHandlerType<?> serverGuiType) {
        return null;
    }

    @Override
    public BlockPoly getBlockPoly(Block block) {
        return null;
    }

    @Override
    public <T extends Entity> EntityPoly<T> getEntityPoly(EntityType<T> entity) {
        return null;
    }

    @Override
    public ItemStack reverseClientItem(ItemStack clientItem) {
        return clientItem;
    }

    @Override
    public boolean isVanillaLikeMap() {
        return false; //This disables patches meant for vanilla clients
    }

    @Override
    public boolean hasBlockWizards() {
        return false;
    }

    @Override
    public boolean shouldForceBlockStateSync(World world, BlockState sourceState, BlockPos sourcePos, BlockPos oppositePos, BlockState clientState, Direction direction) {
        return false;
    }

    @Override
    public @Nullable PolyMcResourcePack generateResourcePack(SimpleLogger logger) {
        return null;
    }

    @Override
    public String dumpDebugInfo() {
        return "";
    }
}
