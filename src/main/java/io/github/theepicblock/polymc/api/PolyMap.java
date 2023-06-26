/*
 * PolyMc
 * Copyright (C) 2020-2020 TheEpicBlock_TEB
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; If not, see <https://www.gnu.org/licenses>.
 */
package io.github.theepicblock.polymc.api;

import io.github.theepicblock.polymc.api.block.BlockPoly;
import io.github.theepicblock.polymc.api.block.WizardConstructor;
import io.github.theepicblock.polymc.api.entity.EntityPoly;
import io.github.theepicblock.polymc.api.gui.GuiPoly;
import io.github.theepicblock.polymc.api.item.ItemLocation;
import io.github.theepicblock.polymc.api.item.ItemPoly;
import io.github.theepicblock.polymc.api.resource.PolyMcResourcePack;
import io.github.theepicblock.polymc.api.wizard.Wizard;
import io.github.theepicblock.polymc.api.wizard.WizardInfo;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.misc.logging.SimpleLogger;
import io.github.theepicblock.polymc.mixins.entity.EntityAttributesFilteringMixin;
import io.github.theepicblock.polymc.mixins.gui.GuiPolyImplementation;
import io.github.theepicblock.polymc.mixins.item.CreativeItemStackFix;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PolyMap {
    /**
     * Converts the serverside representation of an item into a clientside one that should be sent to the client.
     */
    ItemStack getClientItem(ItemStack serverItem, @Nullable ServerPlayerEntity player, @Nullable ItemLocation location);

    /**
     * Converts the serverside representation of a block into a clientside one that should be sent to the client.
     */
    BlockState getClientState(@NotNull BlockState serverBlock, @Nullable ServerPlayerEntity player);

    /**
     * Get the raw id of the clientside blockstate.
     */
    @ApiStatus.Internal
    default int getClientStateRawId(@NotNull BlockState state, ServerPlayerEntity playerEntity) {
        BlockState clientState = this.getClientState(state, playerEntity);

        if (clientState == null) {
            clientState = Blocks.STONE.getDefaultState();
        }

        return Block.STATE_IDS.getRawId(clientState);
    }

    @Nullable WizardConstructor getWizardConstructor(@NotNull BlockState state);

    /**
     * @return the {@link ItemPoly} that this PolyMap associates with this {@link Item}.
     */
    ItemPoly getItemPoly(Item item);

    /**
     * @return the {@link BlockPoly} that this PolyMap associates with this {@link Block}.
     */
    @Deprecated
    default BlockPoly getBlockPoly(@NotNull Block block) {
        return new BlockPoly() {
            @Override
            public BlockState getClientBlock(BlockState input) {
                return this.getClientBlock(input);
            }

            @Override
            public boolean hasWizard() {
                return PolyMap.this.hasBlockWizards();
            }

            @Override
            public Wizard createWizard(WizardInfo info) {
                // TODO
                return null;
            }
        };
    }

    /**
     * @return the {@link GuiPoly} that this PolyMap associates with this {@link ScreenHandlerType}.
     */
    GuiPoly getGuiPoly(ScreenHandlerType<?> serverGuiType);

    /**
     * @return the {@link EntityPoly} that this PolyMap associates with this {@link EntityType}.
     */
    <T extends Entity> EntityPoly<T> getEntityPoly(EntityType<T> entity);

    /**
     * Reverts the clientside item into the serverside representation.
     * This should be the reverse of {@link #getClientItem(ItemStack, ServerPlayerEntity, ItemLocation)}.
     * For optimization reasons, this method only needs to be implemented for items gained by players in creative mode.
     * @see CreativeItemStackFix
     */
    ItemStack reverseClientItem(ItemStack clientItem);

    /**
     * Specifies if this map is meant for vanilla-like clients
     * This is used to disable/enable miscellaneous patches
     * @see io.github.theepicblock.polymc.mixins.CustomPacketDisabler
     * @see io.github.theepicblock.polymc.mixins.block.ResyncImplementation
     * @see io.github.theepicblock.polymc.impl.mixin.CustomBlockBreakingCheck#needsCustomBreaking(ServerPlayerEntity, BlockState)
     * @see GuiPolyImplementation
     * @see io.github.theepicblock.polymc.mixins.item.CustomRecipeFix
     */
    boolean isVanillaLikeMap();

    boolean hasBlockWizards();

    /**
     * Specifies if the {@link BlockState} changes done around this block might require a resync.
     */
    boolean shouldForceBlockStateSync(BlockState sourceState, BlockState clientState, Direction direction);

    @Nullable PolyMcResourcePack generateResourcePack(SimpleLogger logger);

    String dumpDebugInfo();

    /**
     * Used for filtering out attributes unsupported by client.
     * @see EntityAttributesFilteringMixin
     */
    default boolean canReceiveEntityAttribute(EntityAttribute attribute) {
        return Util.isVanilla(Registries.ATTRIBUTE.getId(attribute));
    }
}
