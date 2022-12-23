package nl.theepicblock.polymc.testmod.automated;

import io.github.theepicblock.polymc.impl.NOPPolyMap;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.test.CustomTestProvider;
import net.minecraft.test.TestFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import nl.theepicblock.polymc.testmod.Testmod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class BlockTests implements FabricGameTest {
    @CustomTestProvider
    public Collection<TestFunction> testItem() {
        var list = new ArrayList<TestFunction>();
        // Different ways in which we can test itemstacks being transformed by PolyMc
        var reserializationMethods = new HashMap<String, ReserializationMethod>();
        reserializationMethods.put("reencode", this::reencodeMethod);
        // Doesn't have enough time to capture. Needs to wait a tick
//        reserializationMethods.put("place", this::placeBlockMethod);

        for (var block : new Block[]{Blocks.DIRT, Testmod.TEST_BLOCK}) {
            var shouldItemBePolyd = block == Testmod.TEST_BLOCK; // Sticks shouldn't be polyd
            for (var nopMap : new Boolean[]{false, true}) {
                if (nopMap) shouldItemBePolyd = false; // If the map is NOP, don't poly regardless

                for (var method : reserializationMethods.entrySet()) {
                    boolean finalShouldItemBePolyd = shouldItemBePolyd;
                    list.add(new TestFunction(
                            "defaultBatch",
                            String.format("blocktests (%s, %s, %s)", block.getTranslationKey(), nopMap, method.getKey()),
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

                                var originalState = block.getDefaultState();
                                var newState = method.getValue().reserialize(originalState, packetCtx);

                                if (finalShouldItemBePolyd) {
                                    ctx.assertTrue(newState != originalState, "Item should've been transformed by PolyMc. Result: "+newState);
                                } else {
                                    ctx.assertTrue(newState == originalState, "Item shouldn't have been transformed by PolyMc. Result: "+newState);
                                }

                                packetCtx.close();
                                ctx.complete();
                            }
                    ));
                }
            }
        }

        return list;
    }

    public BlockState reencodeMethod(BlockState state, PacketTester ctx) {
        return ctx.reencode(new BlockUpdateS2CPacket(new BlockPos(0,0,0), state)).getState();
    }

    public BlockState placeBlockMethod(BlockState state, PacketTester ctx) {
        return ctx.capture(BlockUpdateS2CPacket.class, () -> {
            ctx.getTestContext().setBlockState(BlockPos.ORIGIN, state);
        }).getState();
    }

    public interface ReserializationMethod {
        BlockState reserialize(BlockState state, PacketTester ctx);
    }
}
