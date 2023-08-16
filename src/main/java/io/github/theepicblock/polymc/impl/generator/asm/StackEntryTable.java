package io.github.theepicblock.polymc.impl.generator.asm;

import io.github.theepicblock.polymc.impl.generator.asm.stack.StackEntry;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;

import java.io.*;

public class StackEntryTable {
    private final Reference2IntMap<StackEntry> hashCodeCache = new Reference2IntOpenHashMap<>();
    private final Int2ObjectMap<PacketByteBuf> table = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<StackEntry> readCache = new Int2ObjectOpenHashMap<>();
    private boolean closed = false;

    public void writeEntry(PacketByteBuf buf, StackEntry e) {
        if (closed) throw new UnsupportedOperationException();
        var hashcode = hashCodeCache.computeIfAbsent(e, Object::hashCode);

        // Write the actual entry into the table (might cause more entries to be written)
        if (!table.containsKey(hashcode)) {
            var entrySerialized = PacketByteBufs.create();
            e.writeWithTag(entrySerialized, this);
            table.put(hashcode, entrySerialized);
        }

        // And leave a reference to its position
        buf.writeVarInt(hashcode);
    }

    /**
     * @apiNote recursive reading is only allowed inside the {@link StackEntry#finalizeRead(PacketByteBuf, StackEntryTable)} function.
     */
    public StackEntry readEntry(PacketByteBuf buf) {
        var hashCode = buf.readVarInt();

        var entry = readCache.get(hashCode);
        if (entry == null) {
            var entryBuf = table.get(hashCode);

            entry = StackEntry.readWithTagInner(entryBuf, this);
            readCache.put(hashCode, entry);

            entry.finalizeRead(entryBuf, this);
        }

        return entry;
    }

    public void writeTable(OutputStream stream) throws IOException {
        this.closed = true;
        var writer = new DataOutputStream(stream);
        writer.writeLong(Integer.toUnsignedLong(table.size()));

        for (var entry : table.int2ObjectEntrySet()) {
            var hashcode = entry.getIntKey();
            var entryBytes = entry.getValue();

            writer.writeInt(hashcode);
            writer.writeInt(entryBytes.readableBytes());
            entryBytes.readBytes(writer, entryBytes.readableBytes());
            entryBytes.resetReaderIndex();
        }
    }

    public static StackEntryTable readTable(InputStream stream) throws IOException {
        var n = new StackEntryTable();
        var reader = new DataInputStream(stream);

        var size = reader.readLong();
        for (int i = 0; i < size; i++) {
            var hashCode = reader.readInt();
            var bufSize = reader.readInt();
            var buf = PacketByteBufs.create();
            buf.writeBytes(reader, bufSize);

            n.table.put(hashCode, buf);
        }

        return n;
    }
}
