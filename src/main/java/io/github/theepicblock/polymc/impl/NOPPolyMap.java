package io.github.theepicblock.polymc.impl;

import com.google.common.collect.ImmutableMap;
import io.github.theepicblock.polymc.api.PolyMap;
import io.github.theepicblock.polymc.api.block.BlockPoly;
import io.github.theepicblock.polymc.api.entity.EntityPoly;
import io.github.theepicblock.polymc.api.gui.GuiPoly;
import io.github.theepicblock.polymc.api.item.ItemPoly;
import io.github.theepicblock.polymc.impl.poly.item.ArmorMaterialPoly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public class NOPPolyMap implements PolyMap {
    @Override
    public ItemStack getClientItem(ItemStack serverItem, @Nullable ServerPlayerEntity player) {
        return serverItem;
    }

    @Override
    public BlockState getClientBlock(BlockState serverBlock) {
        return serverBlock;
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
    public ImmutableMap<Item,ItemPoly> getItemPolys() {
        return null;
    }

    @Override
    public ImmutableMap<Block,BlockPoly> getBlockPolys() {
        return null;
    }

    @Override
    public ImmutableMap<ArmorMaterial, ArmorMaterialPoly> getArmorMaterialPolys() {
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
}
