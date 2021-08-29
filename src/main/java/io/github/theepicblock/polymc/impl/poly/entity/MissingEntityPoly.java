package io.github.theepicblock.polymc.impl.poly.entity;

import io.github.theepicblock.polymc.api.entity.EntityPoly;
import io.github.theepicblock.polymc.api.wizard.VItem;
import io.github.theepicblock.polymc.api.wizard.Wizard;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;

public class MissingEntityPoly<T extends Entity> implements EntityPoly<T> {
    @Override
    public Wizard createWizard(ServerWorld world, Vec3d pos, T entity) {
        return new MissingEntityWizard<>(world, pos, entity);
    }

    public static class MissingEntityWizard<T extends Entity> extends EntityWizard<T> {
        private static final ItemStack ITEM = new ItemStack(Items.RED_STAINED_GLASS_PANE);
        private final VItem item;
        private final ArrayList<ServerPlayerEntity> players = new ArrayList<>();

        public MissingEntityWizard(ServerWorld world, Vec3d position, T entity) {
            super(world, position, entity);
            item = new VItem();
        }

        @Override
        public void onMove() {
            players.forEach((player) -> item.move(player, this.getPosition(), (byte)0, (byte)0, true));
            super.onMove();
        }

        @Override
        public void addPlayer(ServerPlayerEntity playerEntity) {
            players.add(playerEntity);
            item.spawn(playerEntity, this.getPosition());
            item.setNoGravity(playerEntity, true);
            item.sendItem(playerEntity, ITEM);
        }

        @Override
        public void removePlayer(ServerPlayerEntity playerEntity) {
            players.remove(playerEntity);
            item.remove(playerEntity);
        }
    }
}
