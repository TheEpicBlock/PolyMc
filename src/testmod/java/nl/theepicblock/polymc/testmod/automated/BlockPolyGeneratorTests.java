package nl.theepicblock.polymc.testmod.automated;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.*;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Properties;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import nl.theepicblock.polymc.testmod.Testmod;

import java.util.function.Predicate;

import static nl.theepicblock.polymc.testmod.automated.TestUtil.assertTrue;

public class BlockPolyGeneratorTests implements FabricGameTest {
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testDoor(TestContext ctx) {
        assertPoly(
                ctx,
                Testmod.TEST_DOOR,
                result -> (result.getBlock() instanceof DoorBlock && opensAfterRightClick(ctx, result)),
                "should be a door that's openable"
        );
        assertPoly(
                ctx,
                Testmod.TEST_IRON_DOOR,
                result -> result.getBlock() == Blocks.IRON_DOOR,
                "as of 1.20.1, doors that can't be opened by hand (such as Testmod.TEST_IRON_DOOR), should only be polied with minecraft:iron_door"
        );
        ctx.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testTrapDoor(TestContext ctx) {
        assertPoly(
                ctx,
                Testmod.TEST_TRAP_DOOR,
                result -> (result.getBlock() instanceof TrapdoorBlock && opensAfterRightClick(ctx, result)),
                "should be a trap door that's openable"
        );
        assertPoly(
                ctx,
                Testmod.TEST_IRON_TRAP_DOOR,
                result -> result.getBlock() == Blocks.IRON_TRAPDOOR,
                "as of 1.20.1, doors that can't be opened by hand (such as Testmod.TEST_IRON_DOOR), should only be polied with minecraft:iron_trapdoor"
        );
        ctx.complete();
    }

    public static void assertPoly(TestContext ctx, Block a, Predicate<BlockState> check, String message) {
        var poly = TestUtil.getMap().getBlockPoly(a);
        var polied = poly.getClientBlock(a.getDefaultState());
        assertTrue(check.test(polied), Registries.BLOCK.getId(a)+" didn't get polied correctly: "+message+ " found "+polied+" instead");
    }

    public static boolean opensAfterRightClick(TestContext ctx, BlockState a) {
        var startOpenedState = a.get(Properties.OPEN);
        ctx.setBlockState(BlockPos.ORIGIN, a);
        ctx.useBlock(BlockPos.ORIGIN);
        var endOpenedState = ctx.getBlockState(BlockPos.ORIGIN).get(Properties.OPEN);
        return startOpenedState != endOpenedState;
    }
}
