package nl.theepicblock.polymc.testmod.automated;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.s2c.play.WorldEventS2CPacket;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import nl.theepicblock.polymc.testmod.Testmod;

public class MiscTests implements FabricGameTest {
    // These tests are broken, I am aware

    /**
     * What something like PolyFactory will do
     * Notice how the breaker is null
     * @see io.github.theepicblock.polymc.mixins.block.implementations.BreakParticleImplementation
     */
    @GameTest(templateName = EMPTY_STRUCTURE,  batchId = "breakSimulated")
    public void breakNull(TestContext ctx) {
        var packetCtx = new PacketTester(ctx);
        var block = Testmod.TEST_BLOCK;
        var state = block.getDefaultState();

        block.onBreak(ctx.getWorld(), ctx.getAbsolutePos(BlockPos.ORIGIN), state, null);

        var p = packetCtx.getFirstOfType(WorldEventS2CPacket.class);
        ctx.assertTrue(p.getEventId() == 2001, "Breaking a block should send a break particle packet");

        ctx.complete();
        packetCtx.close();
    }

    /**
     * Have the fake player break a modded block
     * it should still send a packet
     * @see io.github.theepicblock.polymc.mixins.block.implementations.BreakParticleImplementation
     */
    @GameTest(templateName = EMPTY_STRUCTURE,  batchId = "breakSimulated")
    public void breakModded(TestContext ctx) {
        var packetCtx = new PacketTester(ctx);
        var block = Testmod.TEST_BLOCK;
        var state = block.getDefaultState();
        block.onBreak(ctx.getWorld(), ctx.getAbsolutePos(BlockPos.ORIGIN), state, packetCtx.playerEntity);

        var p = packetCtx.getFirstOfType(WorldEventS2CPacket.class);
        ctx.assertTrue(p.getEventId() == 2001, "Breaking a modded block should still send a packet even if you're the breaker");

        ctx.complete();
        packetCtx.close();
    }

    /**
     * Have the fake player break a normal block
     * it should have the vanilla behaviour of not sending a packet
     * @see io.github.theepicblock.polymc.mixins.block.implementations.BreakParticleImplementation
     */
    @GameTest(templateName = EMPTY_STRUCTURE,  batchId = "breakSimulated")
    public void breakModdedVanilla(TestContext ctx) {
        var packetCtx = new PacketTester(ctx);
        var block = Blocks.GRASS;
        var state = block.getDefaultState();

        block.onBreak(ctx.getWorld(), ctx.getAbsolutePos(BlockPos.ORIGIN), state, packetCtx.playerEntity);

        var p = packetCtx.getReceived(WorldEventS2CPacket.class);
        ctx.assertTrue(p.findAny().isEmpty(), "Breaking a vanilla block should have vanilla behaviour");

        ctx.complete();
        packetCtx.close();
    }
}
