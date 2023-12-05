package nl.theepicblock.polymc.testmod.automated;

import io.github.theepicblock.polymc.impl.NOPPolyMap;
import io.github.theepicblock.polymc.mixins.wizards.ItemEntityAccessor;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.ItemEntity;
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
import java.util.Objects;

public class ItemTests implements FabricGameTest {
    @CustomTestProvider
    public Collection<TestFunction> testItem() {
        var list = new ArrayList<TestFunction>();
        // Different ways in which we can test itemstacks being transformed by PolyMc
        var reserializationMethods = new HashMap<String, ReserializationMethod>();
        reserializationMethods.put("reencode", this::reencodeMethod);
        reserializationMethods.put("item entity", this::itemEntityMethod);

        var i = 0;
        for (var isItemVanilla : new boolean[]{true, false}) {
            for (var useNopMap : new Boolean[]{false, true}) {
                for (var method : reserializationMethods.entrySet()) {
                    var item = isItemVanilla ? Items.STICK : Testmod.TEST_ITEM;
                    list.add(new TestFunction(
                            "itembatch_"+i++,
                            String.format("itemtests (%s, %s, %s)", item.getTranslationKey(), useNopMap, method.getKey()),
                            EMPTY_STRUCTURE,
                            100,
                            0,
                            true,
                            (ctx) -> {
                                // The actual test function
                                var packetCtx = new PacketTester(ctx);
                                if (useNopMap) {
                                    packetCtx.setMap(new NOPPolyMap());
                                }

                                var originalStack = new ItemStack(item);
                                originalStack.setCount(5);
                                var copyOfOriginal = originalStack.copy();

                                var newStack = method.getValue().reserialize(originalStack, packetCtx);

                                if (isItemVanilla || useNopMap) {
                                    ctx.assertTrue(newStack.getItem() == originalStack.getItem(), "Item shouldn't have been transformed by PolyMc. Result: "+newStack);
                                } else {
                                    ctx.assertTrue(newStack.getItem() != originalStack.getItem(), "Item should've been transformed by PolyMc. Result: "+newStack);
                                }
                                ctx.assertTrue(newStack.getCount() == 5, "PolyMc shouldn't affect itemcount");
                                ctx.assertTrue(ItemStack.areItemsEqual(originalStack, copyOfOriginal), "PolyMc shouldn't affect the original item");
                                ctx.assertTrue(Objects.equals(originalStack.getNbt(), copyOfOriginal.getNbt()), "PolyMc shouldn't affect the original item's nbt");

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
        return ctx.reencode(new ScreenHandlerSlotUpdateS2CPacket(0,0,0, stack)).getStack();
    }

    public ItemStack itemEntityMethod(ItemStack stack, PacketTester ctx) {
        var coords = ctx.getTestContext().getAbsolute(new Vec3d(0,0,0));
        var entity = new ItemEntity(ctx.getTestContext().getWorld(), coords.x, coords.y, coords.z, stack);

        var trackerPackets = ctx.captureAll(() -> {
            ctx.getTestContext().getWorld().spawnEntity(entity);
            ctx.getTestContext().getWorld().tick(() -> false); // Tick the world so packets are sent
        });

        var entry = trackerPackets.stream()
                .filter(p -> p instanceof EntityTrackerUpdateS2CPacket)
                .map(p -> (EntityTrackerUpdateS2CPacket)p)
                .filter(p -> p.id() == entity.getId())
                .flatMap(p -> p.trackedValues().stream())
                .filter(p -> p.id() == ItemEntityAccessor.getStackTracker().getId())
                .findAny();

        return (ItemStack)(entry.orElseThrow().value());
    }

    public interface ReserializationMethod {
        ItemStack reserialize(ItemStack stack, PacketTester ctx);
    }
}
