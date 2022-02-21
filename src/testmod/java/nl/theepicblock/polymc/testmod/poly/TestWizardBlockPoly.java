package nl.theepicblock.polymc.testmod.poly;

import io.github.theepicblock.polymc.api.block.BlockPoly;
import io.github.theepicblock.polymc.api.wizard.VItem;
import io.github.theepicblock.polymc.api.wizard.Wizard;
import io.github.theepicblock.polymc.api.wizard.WizardInfo;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;

public class TestWizardBlockPoly implements BlockPoly {
    @Override
    public BlockState getClientBlock(BlockState input) {
        return Blocks.RED_STAINED_GLASS.getDefaultState();
    }

    @Override
    public boolean hasWizard() {
        return true;
    }

    @Override
    public Wizard createWizard(WizardInfo info) {
        return new TestWizard(info);
    }

    public static class TestWizard extends Wizard {
        private static final ItemStack ITEM = new ItemStack(Items.DIAMOND);
        private final VItem item;

        public TestWizard(WizardInfo info) {
            super(info);
            item = new VItem();
        }

        @Override
        public void onMove() {
            this.getPlayersWatchingChunk().forEach((player) -> item.move(player, this.getPosition(), (byte)0, (byte)0, true));
            super.onMove();
        }

        @Override
        public void onTick() {
            this.getPlayersWatchingChunk().forEach(player -> {
                player.networkHandler.sendPacket(new ParticleS2CPacket(ParticleTypes.WAX_ON,
                        false,
                        this.getPosition().x,
                        this.getPosition().y+0.5,
                        this.getPosition().z,
                        0, 0, 0, 0, 0));
            });
        }

        @Override
        public boolean needsTicking() {
            return true;
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
