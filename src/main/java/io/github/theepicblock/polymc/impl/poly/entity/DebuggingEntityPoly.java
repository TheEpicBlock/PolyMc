package io.github.theepicblock.polymc.impl.poly.entity;

import io.github.theepicblock.polymc.api.entity.EntityPoly;
import io.github.theepicblock.polymc.api.wizard.VItemFrame;
import io.github.theepicblock.polymc.api.wizard.Wizard;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;

public class DebuggingEntityPoly<T extends Entity> implements EntityPoly<T> {
    @Override
    public Wizard createWizard(ServerWorld world, Vec3d pos, T entity) {
        return new DebuggingEntityWizard<>(world, pos, entity);
    }

    public static class DebuggingEntityWizard<T extends Entity> extends EntityWizard<T> {
        private final VItemFrame itemFrame;
        private final ArrayList<ServerPlayerEntity> players = new ArrayList<>();

        public DebuggingEntityWizard(ServerWorld world, Vec3d position, T entity) {
            super(world, position, entity);
            itemFrame = new VItemFrame();
        }

        @Override
        public void onMove() {
            players.forEach((player) -> itemFrame.move(player, this.getPosition(), (byte)0, (byte)0, false));
            super.onMove();
        }

        @Override
        public void addPlayer(ServerPlayerEntity playerEntity) {
            players.add(playerEntity);
            itemFrame.spawn(playerEntity, this.getPosition());
        }

        @Override
        public void removePlayer(ServerPlayerEntity playerEntity) {
            players.remove(playerEntity);
            itemFrame.remove(playerEntity);
        }
    }
}
