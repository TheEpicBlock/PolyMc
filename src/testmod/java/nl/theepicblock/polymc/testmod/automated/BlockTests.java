package nl.theepicblock.polymc.testmod.automated;

import io.github.theepicblock.polymc.impl.NOPPolyMap;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.test.CustomTestProvider;
import net.minecraft.test.TestContext;
import net.minecraft.test.TestFunction;
import net.minecraft.util.math.BlockPos;
import nl.theepicblock.polymc.testmod.Testmod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.function.Consumer;

import static nl.theepicblock.polymc.testmod.automated.TestUtil.assertDifferent;
import static nl.theepicblock.polymc.testmod.automated.TestUtil.assertEq;

public class BlockTests implements FabricGameTest {
    @CustomTestProvider
    public Collection<TestFunction> testItem() {
        var list = new ArrayList<TestFunction>();
        // Different ways in which we can test itemstacks being transformed by PolyMc
        var reserializationMethods = new HashMap<String, ReserializationMethod>();
        reserializationMethods.put("reencode", this::reencodeMethod);
//        reserializationMethods.put("place", this::placeBlockMethod); // Too flaky

        var i = 0;
        for (var isBlockVanilla : new boolean[]{true, false}) {
            for (var useNopMap : new Boolean[]{false, true}) {
                for (var method : reserializationMethods.entrySet()) {
                    var block = isBlockVanilla ? Blocks.DIRT : Testmod.TEST_BLOCK;
                    list.add(TestUtil.testBuilder()
                            .batch("blockbatch_"+i++)
                            .name(String.format("%s, %s, %s", isBlockVanilla, useNopMap, method.getKey()))
                            .func((ctx) -> runBlockTest(ctx, block, method.getValue(), isBlockVanilla, useNopMap))
                            .build());
                }
            }
        }

        return list;
    }

    private static void runBlockTest(TestContext ctx, Block block, ReserializationMethod method, boolean isBlockVanilla, boolean useNopMap) {
        var packetCtx = new PacketTester(ctx);
        if (useNopMap) {
            packetCtx.setMap(new NOPPolyMap());
        }

        var originalState = block.getDefaultState();
        method.reserialize(originalState, packetCtx, newState -> {
            if (isBlockVanilla || useNopMap) {
                assertEq(newState == originalState, "Item shouldn't have been transformed by PolyMc");
            } else {
                assertDifferent(newState != originalState, "Item should've been transformed by PolyMc");
            }

            packetCtx.close();
            ctx.complete();
        });
    }


    public void reencodeMethod(BlockState state, PacketTester ctx, Consumer<BlockState> newStateConsumer) {
        newStateConsumer.accept(
                ctx.reencode(new BlockUpdateS2CPacket(new BlockPos(0,0,0), state)).getState()
        );
    }

    public void placeBlockMethod(BlockState state, PacketTester ctx, Consumer<BlockState> newStateConsumer) {
        // This test actually places a block and ensures the packet comes out right on the other end
        // Might be a bit flaky though…
        ctx.getTestContext().waitAndRun(1, () -> {
            ctx.clearPackets();
            ctx.getTestContext().setBlockState(BlockPos.ORIGIN, state);
            ctx.getTestContext().waitAndRun(1, () -> {
                var packet = ctx.getFirstOfType(BlockUpdateS2CPacket.class);
                newStateConsumer.accept(packet.getState());
            });
        });
    }

    public interface ReserializationMethod {
        void reserialize(BlockState state, PacketTester ctx, Consumer<BlockState> newStateConsumer);
    }
}
