package nl.theepicblock.polymc.testmod.automated;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.block.PistonBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import nl.theepicblock.polymc.testmod.Testmod;

public class WizardTests implements FabricGameTest {
    @GameTest(templateName = EMPTY_STRUCTURE, batchId = "blockWizard")
    public void testBlock(TestContext ctx) {
        var packetCtx = new PacketTester(ctx);

        ctx.setBlockState(0,0,0, Testmod.TEST_BLOCK_WIZARD);

        ctx.waitAndRun(2, () -> {
            // Wait a tick so packets can be sent
            var packet = packetCtx.getFirstOfType(EntitySpawnS2CPacket.class);
            ctx.assertTrue(packet.getEntityType() == EntityType.ITEM, "Test wizard should spawn an entity, not a "+packet.getEntityType());

            packetCtx.close();
            ctx.complete();
        });
    }

    @GameTest(templateName = EMPTY_STRUCTURE, batchId = "pistonExtension")
    public void testPiston(TestContext ctx) {
        var packetCtx = new PacketTester(ctx);

        // Set up a piston
        ctx.setBlockState(0,1,0, Blocks.PISTON.getDefaultState().with(PistonBlock.FACING, Direction.UP));
        ctx.setBlockState(0,2,0, Testmod.TEST_BLOCK_WIZARD);

        ctx.runAtTick(1, () -> {
            // Allow for one tick so the previous packets can be sent.
            ctx.setBlockState(0,0,0, Blocks.REDSTONE_BLOCK);
            packetCtx.clearPackets();
        });

        ctx.runAtTick(3, () -> {
            // Piston should be extending now
            ctx.checkBlock(new BlockPos(0,2,0), block -> block == Blocks.MOVING_PISTON, "Piston isn't extending");

            // As part of the extension animation, there should be an item at the midway
            // point in-between the piston extension from y2-3
            var expectedPosition = ctx.getAbsolute(new Vec3d(0.5, 2.5, 0.5));

            ctx.assertTrue(packetCtx.getReceived(EntityPositionS2CPacket.class).anyMatch(packet ->
                            packet.getX() == expectedPosition.getX() &&
                            packet.getY() == expectedPosition.getY() &&
                            packet.getZ() == expectedPosition.getZ()),
                    "Item did not move to the midway point during piston animation");

            packetCtx.close();
            ctx.complete();
        });
    }
}
