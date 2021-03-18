package io.github.theepicblock.polymc.impl.poly;

import io.github.theepicblock.polymc.api.block.BlockPoly;
import io.github.theepicblock.polymc.api.block.BlockWizard;
import io.github.theepicblock.polymc.api.resource.ResourcePackMaker;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

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
    public BlockWizard createWizard(Vec3d pos) {
        return new DebugWizard(original, pos);
    }

    @Override
    public boolean hasWizard() {
        return true;
    }

    public class DebugWizard extends BlockWizard {
        private final Block original;
        private final List<ServerPlayerEntity> players = new ArrayList<>();

        public DebugWizard(Block original, Vec3d position) {
            super(position);
            this.original = original;
        }

        @Override
        public void addPlayer(ServerPlayerEntity playerEntity) {
            System.out.printf("%s at %s: I CAN SEE YOU, %s%n", original.getTranslationKey(), this.getPosition(), playerEntity);
            players.add(playerEntity);
        }

        @Override
        public void removePlayer(ServerPlayerEntity playerEntity) {
            System.out.printf("%s at %s: GOODBYEEEE, %s%n", original.getTranslationKey(), this.getPosition(), playerEntity);
            players.add(playerEntity);
        }

        @Override
        public void removeAllPlayers() {
            players.forEach((player) -> {
                System.out.printf("%s at %s: GOODBYEEEE, %s%n", original.getTranslationKey(), this.getPosition(), player);
            });
        }
    }
}
