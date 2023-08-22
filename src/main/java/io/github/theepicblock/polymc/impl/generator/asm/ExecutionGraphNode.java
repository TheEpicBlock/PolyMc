package io.github.theepicblock.polymc.impl.generator.asm;

import io.github.theepicblock.polymc.impl.generator.asm.stack.StackEntry;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.annotation.Debug;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class ExecutionGraphNode {
    private @Nullable List<RenderCall> calls;
    private IfStatement continuation = null;

    public void setContinuation(IfStatement statement) {
        this.continuation = statement;
    }

    public void addCall(RenderCall call) {
        if (this.continuation != null) {
            throw new IllegalStateException("This node has been continued elsewhere, there shouldn't be calls added to it");
        }
        if (this.calls == null) this.calls = new ArrayList<>();
        this.calls.add(call);
    }

    public IfStatement getContinuation() {
        return continuation;
    }

    @Debug
    public Set<RenderCall> getUniqueCalls() {
        var set = new HashSet<RenderCall>();
        visitContinuation(cont -> {
            if (cont.calls != null) set.addAll(cont.calls);
        });
        return set;
    }

    public void forEachCall(Consumer<RenderCall> consumer) {
        visitContinuation(node -> {
            if (node.calls != null) {
                node.calls.forEach(consumer);
            }
        });
    }

    private int countForks() {
        AtomicInteger counter = new AtomicInteger();
        visitContinuation(cont -> {
            // Only count leaf nodes
            if (cont.continuation == null) {
                counter.addAndGet(1);
            }
        });
        return counter.get();
    }

    public List<RenderCall> getCalls() {
        return this.calls;
    }

    private void visitContinuation(Consumer<ExecutionGraphNode> consumer) {
        consumer.accept(this);
        if (this.continuation != null) {
            continuation.continuationIfFalse.visitContinuation(consumer);
            continuation.continuationIfTrue.visitContinuation(consumer);
        }
    }

    public void simplify() {
        if (this.continuation == null) return;
        var ifTrue = this.continuation.continuationIfTrue;
        var ifFalse = this.continuation.continuationIfFalse;

        if (ifTrue.calls == null || ifTrue.calls.isEmpty()) {
            return;
        }
        if (ifFalse.calls == null || ifFalse.calls.isEmpty()) {
            return;
        }

        var callSet = new ArrayList<>(ifTrue.calls);

        var duplicateCalls = new ArrayList<RenderCall>();

        ifFalse.calls.forEach(call -> {
            if (callSet.contains(call)) {
                duplicateCalls.add(call);
            }
        });

        if (!duplicateCalls.isEmpty()) {
            ifTrue.calls.removeAll(duplicateCalls);
            ifFalse.calls.removeAll(duplicateCalls);
            if (this.calls == null) this.calls = new ArrayList<>();
            this.calls.addAll(duplicateCalls);
        }

        this.tryMerge();
    }

    /**
     * Frees up memory
     */
    public void tryMerge() {
        if (this.continuation == null) return; // Nothing to merge
        if (this.continuation.continuationIfTrue.isEmpty() &&
            this.continuation.continuationIfFalse.isEmpty()) {
            this.continuation = null;
        }
    }

    private boolean isEmpty() {
        return this.continuation == null && (this.calls == null || this.calls.isEmpty());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExecutionGraphNode that = (ExecutionGraphNode)o;
        return Objects.equals(calls, that.calls) && Objects.equals(continuation, that.continuation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(calls, continuation);
    }

    public void write(PacketByteBuf byteBuf, StackEntryTable table) {
        byteBuf.writeCollection(this.calls == null ? List.of() : this.calls, (buf, call) -> call.write(byteBuf, table));
        byteBuf.writeNullable(this.continuation, (buf, stmnt) -> stmnt.write(buf, table));
    }

    public static ExecutionGraphNode read(PacketByteBuf byteBuf, StackEntryTable table) {
        var node = new ExecutionGraphNode();
        node.calls = byteBuf.readList(buf -> RenderCall.read(byteBuf, table));
        node.continuation = byteBuf.readNullable(buf -> IfStatement.read(byteBuf, table));
        return node;
    }

    /**
     * Represents a comparison between two elements (which might not have a concrete value),
     * and the paths that are taken due to it
     */
    public record IfStatement(StackEntry compA, @Nullable StackEntry compB, int opcode, @NotNull ExecutionGraphNode continuationIfFalse, @NotNull ExecutionGraphNode continuationIfTrue) {
        public void write(PacketByteBuf byteBuf, StackEntryTable table) {
            table.writeEntry(byteBuf, compA);
            byteBuf.writeNullable(compB, table::writeEntry);
            byteBuf.writeVarInt(opcode);
            continuationIfFalse.write(byteBuf, table);
            continuationIfTrue.write(byteBuf, table);
        }

        public static IfStatement read(PacketByteBuf byteBuf, StackEntryTable table) {
            return new IfStatement(
                    table.readEntry(byteBuf),
                    byteBuf.readNullable(table::readEntry),
                    byteBuf.readVarInt(),
                    ExecutionGraphNode.read(byteBuf, table),
                    ExecutionGraphNode.read(byteBuf, table)
            );
        }
    }

    public record RenderCall(StackEntry cuboid, StackEntry matrix) {
        public void write(PacketByteBuf byteBuf, StackEntryTable table) {
            table.writeEntry(byteBuf, cuboid);
            table.writeEntry(byteBuf, matrix);
        }

        public static RenderCall read(PacketByteBuf byteBuf, StackEntryTable table) {
            return new RenderCall(
                    table.readEntry(byteBuf),
                    table.readEntry(byteBuf)
            );
        }
    }

    @Override
    public String toString() {
        return "graphnode ["+(this.getCalls() == null ? null : this.getCalls().size())+"]";
    }
}
