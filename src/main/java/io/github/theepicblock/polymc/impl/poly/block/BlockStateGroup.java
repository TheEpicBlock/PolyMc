package io.github.theepicblock.polymc.impl.poly.block;

import io.github.theepicblock.polymc.api.block.BlockStateMerger;
import net.minecraft.block.BlockState;

import java.util.ArrayList;

public class BlockStateGroup {
    private final BlockState neutralizedState;
    private final ArrayList<BlockState> states = new ArrayList<>();

    public BlockStateGroup(BlockState firstState, BlockStateMerger merger) {
        states.add(firstState);
        neutralizedState = merger.neutralize(firstState);
    }

    public boolean add(BlockState state, BlockStateMerger merger) {
        if (merger.neutralize(state).equals(neutralizedState)) {
            states.add(state);
            return true;
        }
        return false;
    }

    public ArrayList<BlockState> getStates() {
        return states;
    }

    public BlockState getNeutralizedState() {
        return neutralizedState;
    }
}
