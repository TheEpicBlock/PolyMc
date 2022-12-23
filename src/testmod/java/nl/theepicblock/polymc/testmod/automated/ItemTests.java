package nl.theepicblock.polymc.testmod.automated;

import io.github.theepicblock.polymc.impl.NOPPolyMap;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.test.CustomTestProvider;
import net.minecraft.test.TestFunction;
import net.minecraft.util.math.Vec3d;
import nl.theepicblock.polymc.testmod.Testmod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class ItemTests implements FabricGameTest {
    @CustomTestProvider
    public Collection<TestFunction> testItem() {
        var list = new ArrayList<TestFunction>();
        // Different ways in which we can test itemstacks being transformed by PolyMc
        var reserializationMethods = new HashMap<String, ReserializationMethod>();
        reserializationMethods.put("reencode", this::reencodeMethod);
        // Packet capture doesn't seem stable enough
        //reserializationMethods.put("item entity", this::itemEntityMethod);

        for (var item : new Item[]{Items.STICK, Testmod.TEST_ITEM}) {
            var shouldItemBePolyd = item == Testmod.TEST_ITEM; // Sticks shouldn't be polyd
            for (var nopMap : new Boolean[]{false, true}) {
                if (nopMap) shouldItemBePolyd = false; // If the map is NOP, don't poly regardless

                for (var method : reserializationMethods.entrySet()) {
                    boolean finalShouldItemBePolyd = shouldItemBePolyd;
                    list.add(new TestFunction(
                            "defaultBatch",
                            String.format("itemtests (%s, %s, %s)", item.getTranslationKey(), nopMap, method.getKey()),
                            EMPTY_STRUCTURE,
                            1,
                            1,
                            true,
                            (ctx) -> {
                                // The actual test function
                                var packetCtx = new PacketTester(ctx);
                                if (nopMap) {
                                    packetCtx.setMap(new NOPPolyMap());
                                }

                                var originalStack = new ItemStack(item);
                                originalStack.setCount(5);
                                var copyOfOriginal = originalStack.copy();

                                var newStack = method.getValue().reserialize(originalStack, packetCtx);

                                if (finalShouldItemBePolyd) {
                                    ctx.assertTrue(newStack.getItem() != originalStack.getItem(), "Item should've been transformed by PolyMc. Result: "+newStack);
                                } else {
                                    ctx.assertTrue(newStack.getItem() == originalStack.getItem(), "Item shouldn't have been transformed by PolyMc. Result: "+newStack);
                                }
                                ctx.assertTrue(newStack.getCount() == 5, "PolyMc shouldn't affect itemcount");
                                ctx.assertTrue(ItemStack.areItemsEqual(originalStack, copyOfOriginal), "PolyMc shouldn't affect the original item");
                                ctx.assertTrue(ItemStack.areNbtEqual(originalStack, copyOfOriginal), "PolyMc shouldn't affect the original item's nbt");

                                packetCtx.close();
                                ctx.complete();
                            }
                    ));
                }
            }
        }

        return list;
    }

    public ItemStack reencodeMethod(ItemStack stack, PacketTester ctx) {
        return ctx.reencode(new ScreenHandlerSlotUpdateS2CPacket(0,0,0, stack)).getItemStack();
    }

    public ItemStack itemEntityMethod(ItemStack stack, PacketTester ctx) {
        var coords = ctx.getTestContext().getAbsolute(new Vec3d(0,0,0));
        var entity = new ItemEntity(ctx.getTestContext().getWorld(), coords.x, coords.y, coords.z, stack);

        var trackerPacket = ctx.capture(EntityTrackerUpdateS2CPacket.class, () -> {
            ctx.getTestContext().getWorld().spawnEntity(entity);
        });

        ctx.getTestContext().assertTrue(trackerPacket.trackedValues().size() == 1, "Weird tracker update");

        return (ItemStack)trackerPacket.trackedValues().get(0).value();
    }

    public interface ReserializationMethod {
        ItemStack reserialize(ItemStack stack, PacketTester ctx);
    }
}
