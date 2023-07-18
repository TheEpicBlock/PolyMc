package io.github.theepicblock.polymc.impl.generator.asm;

import io.github.theepicblock.polymc.impl.generator.asm.stack.StackEntry;
import net.minecraft.util.annotation.Debug;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

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
        visitContinuation(cont -> set.addAll(cont.calls));
        return set;
    }

    private int countParallelUniverses() {
        AtomicInteger counter = new AtomicInteger();
        visitContinuation(cont -> {
            // Only count leaf nodes
            if (cont.continuation == null) {
                counter.addAndGet(1);
            }
        });
        return counter.get();
    }

    private void visitContinuation(Consumer<ExecutionGraphNode> consumer) {
        consumer.accept(this);
        if (this.continuation != null) {
            continuation.continuationIfFalse.visitContinuation(consumer);
            continuation.continuationIfTrue.visitContinuation(consumer);
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
