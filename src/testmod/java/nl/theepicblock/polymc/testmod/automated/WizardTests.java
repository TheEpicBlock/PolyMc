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
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testBlock(TestContext ctx) {
        var packetCtx = new PacketTester(ctx);

        var packet = packetCtx.capture(EntitySpawnS2CPacket.class, () -> {
            ctx.setBlockState(0,0,0, Testmod.TEST_BLOCK_WIZARD);
        });
        ctx.assertTrue(packet.getEntityType() == EntityType.ITEM, "Test wizard should spawn an entity, not a "+packet.getEntityType());

        packetCtx.close();
        ctx.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE, batchId = "pistonExtension")
    public void testPiston(TestContext ctx) {
        var packetCtx = new PacketTester(ctx);

        // Set up a piston
        ctx.setBlockState(0,1,0, Blocks.PISTON.getDefaultState().with(PistonBlock.FACING, Direction.UP));
        ctx.setBlockState(0,2,0, Testmod.TEST_BLOCK_WIZARD);
        ctx.setBlockState(0,0,0, Blocks.REDSTONE_BLOCK);

        packetCtx.clearPackets(); // Start capturing packets
        ctx.waitAndRun(1, () -> {
            // Piston should be extending now
            ctx.checkBlock(new BlockPos(0,2,0), block -> block == Blocks.MOVING_PISTON, "Piston isn't extending");

            var packet = packetCtx.getFirstOfType(EntityPositionS2CPacket.class);
            var expectedPosition = ctx.getAbsolute(new Vec3d(0.5, 2.5, 0.5)); // Midway in-between piston extension from y2-3
            ctx.assertTrue(packet.getX() == expectedPosition.getX(), "Wrong x value: "+packet.getX() + " instead of " + expectedPosition.getX());
            ctx.assertTrue(packet.getY() == expectedPosition.getY(), "Wrong y value: "+packet.getY() + " instead of " + expectedPosition.getY());
            ctx.assertTrue(packet.getZ() == expectedPosition.getZ(), "Wrong z value: "+packet.getZ() + " instead of " + expectedPosition.getZ());
        });

        packetCtx.close();
        ctx.complete();
    }
}
