package io.github.theepicblock.polymc.impl.poly;

import io.github.theepicblock.polymc.api.block.BlockPoly;
import io.github.theepicblock.polymc.api.wizard.Wizard;
import io.github.theepicblock.polymc.api.resource.ResourcePackMaker;
import io.github.theepicblock.polymc.impl.Util;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

//TODO remove once done debugging
public class WizardDebuggingPoly implements BlockPoly {
    private final Block original;

    public WizardDebuggingPoly(Block original) {
        this.original = original;
    }

    @Override
    public BlockState getClientBlock(BlockState input) {
        return Blocks.YELLOW_STAINED_GLASS.getDefaultState();
    }

    @Override
    public void AddToResourcePack(Block block, ResourcePackMaker pack) {}

    @Override
    public Wizard createWizard(Vec3d pos) {
        return new DebugWizard(original, pos);
    }

    @Override
    public boolean hasWizard() {
        return true;
    }

    public static class DebugWizard extends Wizard {
        private final int entityId = Util.getNewEntityId();
        private final UUID uuid = MathHelper.randomUuid(ThreadLocalRandom.current());
        private final Block original;
        private final List<ServerPlayerEntity> players = new ArrayList<>();

        public DebugWizard(Block original, Vec3d position) {
            super(position);
            this.original = original;
        }

        private void add(ServerPlayerEntity player) {
            player.networkHandler.sendPacket(new EntitySpawnS2CPacket(
                    entityId,
                    uuid,
                    this.getPosition().x,
                    this.getPosition().y,
                    this.getPosition().z,
                    0,
                    0,
                    EntityType.MINECART,
                    0,
                    Vec3d.ZERO
            ));
        }

        private void rem(ServerPlayerEntity player) {
            player.networkHandler.sendPacket(new EntitiesDestroyS2CPacket(this.entityId));
        }

        @Override
        public void addPlayer(ServerPlayerEntity playerEntity) {
            add(playerEntity);
            players.add(playerEntity);
        }

        @Override
        public void removePlayer(ServerPlayerEntity playerEntity) {
            rem(playerEntity);
            players.add(playerEntity);
        }

        @Override
        public void removeAllPlayers() {
            players.forEach(this::rem);
        }
    }
}
