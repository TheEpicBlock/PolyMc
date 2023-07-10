package io.github.theepicblock.polymc.impl.generator.asm;

import io.github.theepicblock.polymc.impl.generator.asm.stack.StackEntry;
import net.minecraft.util.annotation.Debug;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExecutionGraphNode {
    private final List<RenderCall> calls = new ArrayList<>();
    private IfStatement continuation = null;

    public void setContinuation(IfStatement statement) {
        this.continuation = statement;
    }

    public void addCall(RenderCall call) {
        if (this.continuation != null) {
            throw new IllegalStateException("This node has been continued elsewhere, there shouldn't be calls added to it");
        }
        this.calls.add(call);
    }

    @Debug
    public Set<RenderCall> getUniqueCalls() {
        var set = new HashSet<RenderCall>();
        getUniqueCalls(set);
        return set;
    }

    private void getUniqueCalls(Set<RenderCall> calls) {
        calls.addAll(this.calls);
        if (this.continuation != null) {
            continuation.continuationIfFalse.getUniqueCalls(calls);
            continuation.continuationIfTrue.getUniqueCalls(calls);
        }
    }

    /**
     * Represents a comparision between two elements (which might not have a concrete value),
     * and the paths that are taken due to it
     */
    public record IfStatement(StackEntry compA, @Nullable StackEntry compB, int opcode, ExecutionGraphNode continuationIfFalse, ExecutionGraphNode continuationIfTrue) {
    }

    public record RenderCall(StackEntry cuboid, StackEntry matrix) {}
}
