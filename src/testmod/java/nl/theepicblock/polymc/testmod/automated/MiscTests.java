package nl.theepicblock.polymc.testmod.automated;

import io.github.theepicblock.polymc.api.item.ItemLocation;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldEventS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import nl.theepicblock.polymc.testmod.Testmod;
import nl.theepicblock.polymc.testmod.YellowStatusEffect;

import java.util.Objects;

import static nl.theepicblock.polymc.testmod.YellowStatusEffect.YELLOW;

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

    private ItemStack testPotion() {
        var serverPotion = new ItemStack(Items.POTION);
        serverPotion.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Registries.POTION.getEntry(Testmod.TEST_POTION_TYPE)));
        return serverPotion;
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void potionItem(TestContext ctx) {
        var map = TestUtil.getMap();

        // Create a potion item
        var serverPotion = testPotion();
        TestUtil.assertEq(serverPotion.get(DataComponentTypes.POTION_CONTENTS).getColor(), YELLOW, "Sanity check");
        var clientPotion = map.getClientItem(serverPotion, null, ItemLocation.INVENTORY);

        try {
            YellowStatusEffect.SIMULATE_UNAVAILABLE = true;
            var clientPotionData = clientPotion.get(DataComponentTypes.POTION_CONTENTS);
            TestUtil.assertNonNull(clientPotionData, "polyd potions should still have potion data");
            TestUtil.assertEq(clientPotionData.getColor(), YELLOW, "potion should be yellow");
            TestUtil.assertEq(clientPotion.getName().getLiteralString(), serverPotion.getName().getLiteralString());

            TestUtil.assertDifferent(serverPotion.get(DataComponentTypes.POTION_CONTENTS).getColor(), YELLOW, "Untransformed item should be broken. This test may be invalid");
        } finally {
            YellowStatusEffect.SIMULATE_UNAVAILABLE = false;
        }
        ctx.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void potionItemTooltip(TestContext ctx) {
        var serverPotion = testPotion();

        // PolyMc makes some assumptions with tooltips
        // namely that the client item will be directly serialized
        var packetTester = new PacketTester(ctx);
        var clientPotion = packetTester.reencode(new InventoryS2CPacket(0,0, DefaultedList.of(), serverPotion)).getCursorStack();

        try {
            YellowStatusEffect.SIMULATE_UNAVAILABLE = true;
            TestUtil.assertTrue(
                    clientPotion.getTooltip(Item.TooltipContext.DEFAULT, null, TooltipType.BASIC)
                            .stream()
                            .map(Object::toString)
                            .filter(Objects::nonNull)
                            .anyMatch(str -> str.contains("effect.polymc-testmod.yellow_effect")),
                    "Tooltip should make some reference to the contained effect");
        } finally {
            YellowStatusEffect.SIMULATE_UNAVAILABLE = false;
        }

        packetTester.close();
        ctx.complete();
    }
}
