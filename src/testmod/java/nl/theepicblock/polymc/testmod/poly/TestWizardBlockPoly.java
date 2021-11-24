package nl.theepicblock.polymc.testmod.poly;

import io.github.theepicblock.polymc.api.block.BlockPoly;
import io.github.theepicblock.polymc.api.resource.ResourcePackMaker;
import io.github.theepicblock.polymc.api.wizard.VItem;
import io.github.theepicblock.polymc.api.wizard.Wizard;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public class TestWizardBlockPoly implements BlockPoly {
    @Override
    public BlockState getClientBlock(BlockState input) {
        return Blocks.RED_STAINED_GLASS.getDefaultState();
    }

    @Override
    public void addToResourcePack(Block block, ResourcePackMaker pack) {

    }

    @Override
    public boolean hasWizard() {
        return true;
    }

    @Override
    public Wizard createWizard(ServerWorld world, Vec3d pos, Wizard.WizardState state) {
        return new TestWizard(world, pos, state);
    }

    public static class TestWizard extends Wizard {
        private static final ItemStack ITEM = new ItemStack(Items.DIAMOND);
        private final VItem item;

        public TestWizard(ServerWorld world, Vec3d position, WizardState state) {
            super(world, position, state);
            item = new VItem();
        }

        @Override
        public void onMove() {
            this.getPlayersWatchingChunk().forEach((player) -> item.move(player, this.getPosition(), (byte)0, (byte)0, true));
            super.onMove();
        }

        @Override
        public void addPlayer(ServerPlayerEntity playerEntity) {
            item.spawn(playerEntity, this.getPosition());
            item.setNoGravity(playerEntity, true);
            item.sendItem(playerEntity, ITEM);
        }

        @Override
        public void removePlayer(ServerPlayerEntity playerEntity) {
            item.remove(playerEntity);
        }
    }
}
