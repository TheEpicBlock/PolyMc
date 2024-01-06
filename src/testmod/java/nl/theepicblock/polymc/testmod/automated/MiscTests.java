package nl.theepicblock.polymc.testmod.automated;

import io.github.theepicblock.polymc.api.item.ItemLocation;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.WorldEventS2CPacket;
import net.minecraft.potion.PotionUtil;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import nl.theepicblock.polymc.testmod.Testmod;

import java.util.Objects;

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
        var block = Blocks.SHORT_GRASS;
        var state = block.getDefaultState();

        block.onBreak(ctx.getWorld(), ctx.getAbsolutePos(BlockPos.ORIGIN), state, packetCtx.playerEntity);

        var p = packetCtx.getReceived(WorldEventS2CPacket.class);
        ctx.assertTrue(p.findAny().isEmpty(), "Breaking a vanilla block should have vanilla behaviour");

        ctx.complete();
        packetCtx.close();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void potionItem(TestContext ctx) {
        var map = TestUtil.getMap();
        var serverPotion = new ItemStack(Items.POTION);
        PotionUtil.setPotion(serverPotion, Testmod.TEST_POTION_TYPE);

        var clientPotion = map.getClientItem(serverPotion, null, ItemLocation.INVENTORY);
        clientPotion.getOrCreateNbt().remove("Potion"); // Anything under this tag can't be understood by vanilla client

        TestUtil.assertEq(PotionUtil.getColor(clientPotion), 0xf4e42c, "potion should be yellow");
        TestUtil.assertEq(clientPotion.getName().getLiteralString(), serverPotion.getName().getLiteralString());

        ctx.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE, required = false)
    public void potionItemTooltip(TestContext ctx) {
        var map = TestUtil.getMap();
        var serverPotion = new ItemStack(Items.POTION);
        PotionUtil.setPotion(serverPotion, Testmod.TEST_POTION_TYPE);

        var clientPotion = map.getClientItem(serverPotion, null, ItemLocation.INVENTORY);
        clientPotion.getOrCreateNbt().remove("Potion"); // Anything under this tag can't be understood by vanilla client

        TestUtil.assertTrue(
                clientPotion.getTooltip(null, TooltipContext.Default.BASIC)
                        .stream()
                        .map(Text::getLiteralString)
                        .filter(Objects::nonNull)
                        .anyMatch(str -> str.contains("test_effect")),
                "Tooltip should make some reference to the contained effect");


        ctx.complete();
    }
}
