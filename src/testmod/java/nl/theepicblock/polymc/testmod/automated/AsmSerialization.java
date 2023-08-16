package nl.theepicblock.polymc.testmod.automated;

import io.github.theepicblock.polymc.impl.generator.asm.ExecutionGraphNode;
import io.github.theepicblock.polymc.impl.generator.asm.StackEntryTable;
import io.github.theepicblock.polymc.impl.generator.asm.stack.*;
import io.github.theepicblock.polymc.impl.generator.asm.stack.ops.*;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import org.objectweb.asm.Type;

import static nl.theepicblock.polymc.testmod.automated.TestUtil.assertEq;
import static nl.theepicblock.polymc.testmod.automated.TestUtil.assertTrue;

public class AsmSerialization implements FabricGameTest {
    public static StackEntry MOCK = new MockedObject(new MockedObject.Root("mock"), null);

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testMock(TestContext ctx) {
        tstSerialization(MOCK);
        ctx.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testKnownArray(TestContext ctx) {
        var e = KnownArray.withLength(0);
        tstSerialization(e);
        var e2 = KnownArray.withLength(1);
        e2.data()[0] = MOCK;
        tstSerialization(e2);
        ctx.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testKnownClazz(TestContext ctx) {
        var e = new KnownClass(Type.INT_TYPE);
        tstSerialization(e);
        ctx.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testKnownDouble(TestContext ctx) {
        var e = new KnownDouble(34);
        tstSerialization(e);
        ctx.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testKnownFloat(TestContext ctx) {
        var e = new KnownFloat(56);
        tstSerialization(e);
        ctx.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testKnownInteger(TestContext ctx) {
        var e = new KnownInteger(87);
        tstSerialization(e);
        ctx.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testKnownLong(TestContext ctx) {
        var e = new KnownLong(14);
        tstSerialization(e);
        ctx.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE, required = false)
    public void testUnknown(TestContext ctx) {
        var e = new UnknownValue("test reason");
        tstSerialization(e);
        ctx.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testUnsafeField(TestContext ctx) {
        var e = new UnsafeFieldReference("myField");
        tstSerialization(e);
        ctx.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testArrayLength(TestContext ctx) {
        var e = new ArrayLength(MOCK);
        tstSerialization(e);
        ctx.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testBinaryOp(TestContext ctx) {
        var e = new BinaryOp(MOCK, MOCK, BinaryOp.Op.ADD, BinaryOp.Type.INT);
        tstSerialization(e);
        ctx.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testCast(TestContext ctx) {
        var e = new Cast(MOCK, Cast.Type.FLOAT, Cast.Type.INTEGER);
        tstSerialization(e);
        ctx.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testConcatWithConstant(TestContext ctx) {
        var e = new ConcatWithConstants(MOCK, "constant");
        tstSerialization(e);
        ctx.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testInstanceOf(TestContext ctx) {
        var e = new InstanceOf(MOCK, "java/lang/String");
        tstSerialization(e);
        ctx.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testMathOp(TestContext ctx) {
        var e = new MathOp(MOCK, MathOp.Op.COS);
        tstSerialization(e);
        ctx.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testNewArray(TestContext ctx) {
        var e = new NewArray(MOCK);
        tstSerialization(e);
        ctx.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testRawBitOp(TestContext ctx) {
        var e = new RawBitOp(MOCK, RawBitOp.Op.Float2Int);
        tstSerialization(e);
        ctx.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testStaticFieldValue(TestContext ctx) {
        var e = new StaticFieldValue("java/lang/String", "SomeField");
        tstSerialization(e);
        ctx.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testRenderCall(TestContext ctx) {
        var node = new ExecutionGraphNode.RenderCall(MOCK, MOCK);
        var table = new StackEntryTable();
        tstSerialization(node, (buf, obj) -> obj.write(buf, table), buf -> ExecutionGraphNode.RenderCall.read(buf, table));
        ctx.complete();
    }

    private <T> void tstSerialization(StackEntry entry) {
        var table = new StackEntryTable();
        tstSerialization(entry, table::writeEntry, table::readEntry);
    }

    private <T> void tstSerialization(T obj, PacketByteBuf.PacketWriter<T> writer, PacketByteBuf.PacketReader<T> reader) {
        var buf = PacketByteBufs.create();
        writer.accept(buf, obj);

        assertTrue(buf.readableBytes() > 0, "Writer should've written at least one byte");

        var reserialized = reader.apply(buf);

        assertEq(reserialized, obj);
    }
}
