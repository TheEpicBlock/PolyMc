package nl.theepicblock.polymc.testmod.automated;

import io.github.theepicblock.polymc.impl.generator.asm.ClientClassLoader;
import io.github.theepicblock.polymc.impl.generator.asm.CowCapableMap;
import io.github.theepicblock.polymc.impl.generator.asm.MethodExecutor;
import io.github.theepicblock.polymc.impl.generator.asm.VirtualMachine;
import io.github.theepicblock.polymc.impl.generator.asm.stack.KnownArray;
import io.github.theepicblock.polymc.impl.generator.asm.stack.KnownVmObject;
import io.github.theepicblock.polymc.impl.generator.asm.stack.StackEntry;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import org.objectweb.asm.Type;
import org.spongepowered.include.com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.HashMap;

import static nl.theepicblock.polymc.testmod.automated.TestUtil.*;

public class CowAndHashcode implements FabricGameTest {
    private static final VirtualMachine.Clazz TstType;

    static {
        try {
            var vm = new VirtualMachine(new ClientClassLoader(), new VirtualMachine.VmConfig() {});
            TstType = vm.getClass("java/lang/Object");
        } catch (MethodExecutor.VmException e) {
            throw new RuntimeException(e);
        }

    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testCowMap(TestContext ctx) throws MethodExecutor.VmException {
        var veryOrigin = new CowCapableMap<String>();
        var origin = veryOrigin.createClone();
        var emptyHashcode = origin.hashCode();
        assertEq(emptyHashcode, new CowCapableMap<String>().hashCode(), "two empty maps should have the same hashcode");

        origin.put("a", StackEntry.known(1));
        origin.put("b", new KnownVmObject(TstType));
        assertEq(origin.get("a"), StackEntry.known(1));
        assertEq(origin.get("b"), new KnownVmObject(TstType));

        assertDifferent(origin, new CowCapableMap<>(), "Filled map shouldn't equal empty one");
        assertDifferent(origin.createClone(), new CowCapableMap<>(), "Copied filled map shouldn't equal empty one");

        var originHash = origin.hashCode();
        assertDifferent(originHash, emptyHashcode, "Hashcode should change after something's added");
        origin.put("a", StackEntry.known(1));
        assertEq(originHash, origin.hashCode(), "Hashcode shouldn't change after the exact same value is inserted");

        var copy = origin.createClone();
        // Check if everything's still equal
        assertEq(origin.get("a"), StackEntry.known(1));
        assertEq(originHash, origin.hashCode(), "Hashcode shouldn't change after copy is made");
        assertEq(copy.get("a"), StackEntry.known(1));
        assertEq(copy, origin);
        assertEq(copy.hashCode(), origin.hashCode(), "Copy should have same hashcode as original");

        copy.put("a", StackEntry.known(2));
        assertEq(origin.get("a"), StackEntry.known(1), "Copy affecting original");
        assertDifferent(copy, origin);
        assertDifferent(copy.hashCode(), origin.hashCode(), "Hashcode should change after something's changed");

        copy.get("b").setField("yeet", StackEntry.known(1));
        assertEq(origin.get("b"), new KnownVmObject(TstType), "Copy affecting original");
        assertDifferent(copy, origin);
        assertDifferent(copy.hashCode(), origin.hashCode(), "Hashcode should change after something's changed");

        // This should still be empty
        assertEq(veryOrigin.get("a"), null);
        assertEq(veryOrigin.get("b"), null);
        assertEq(veryOrigin.get("c"), null);
        assertEq(veryOrigin.get("d"), null);
        assertEq(veryOrigin, new CowCapableMap<>());
        assertEq(veryOrigin.hashCode(), new CowCapableMap<>().hashCode());

        ctx.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testKnownObjectRecursion(TestContext ctx) throws MethodExecutor.VmException {
        var obj = new KnownVmObject(TstType);
        obj.setField("a", obj);
        assertEq(obj.getField("a"), obj);

        var prevHash = obj.hashCode();
        var copy = obj.copy();
        assertEq(obj, copy);
        assertEq(obj.hashCode(), copy.hashCode());

        // Set "b" to "1" on the inner ref
        copy.getField("a").setField("b", StackEntry.known(1));
        // It should appear on the outer
        assertEq(copy.getField("b"), StackEntry.known(1));
        // And it shouldn't appear on the original
        assertDifferent(obj.getField("b"), StackEntry.known(1));
        // In fact, the original should be completely unaffected
        assertEq(obj.getField("a"), obj);
        assertEq(obj.hashCode(), prevHash);

        // Object with 1 in-between
        var a = new KnownVmObject(TstType);
        var b = new KnownVmObject(TstType);
        b.setField("myA", a);
        a.setField("myB", b);

        assertEq(a, a.copy());
        assertEq(a.hashCode(), a.copy().hashCode());

        ctx.complete();
    }
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testCowMapIter(TestContext ctx) throws MethodExecutor.VmException {
        var ONE = StackEntry.known(1);
        var TWO = StackEntry.known(2);

        var map = new CowCapableMap<String>();
        var clone = map.createClone();

        map.put("a", ONE);
        clone.put("b", ONE);
        map.put("c", ONE);
        clone.put("c", TWO);

        var mapExpected = ImmutableMap.builder()
                .put("a", ONE)
                .put("c", ONE)
                .build();
        var mapCol = new HashMap<String, StackEntry>();
        map.forEachImmutable(mapCol::put);
        assertEq(mapCol, mapExpected);

        var cloneExpected = ImmutableMap.builder()
                .put("b", ONE)
                .put("c", TWO)
                .build();
        var cloneCol = new HashMap<String, StackEntry>();
        clone.forEachImmutable(cloneCol::put);
        assertEq(cloneCol, cloneExpected);

        ctx.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testCowMapFind(TestContext ctx) throws MethodExecutor.VmException {
        var ONE = StackEntry.known(1);
        var TWO = StackEntry.known(2);

        var map = new CowCapableMap<String>();

        map.put("c", ONE);
        var clone = map.createClone();
        map.put("a", ONE);
        clone.put("b", ONE);
        clone.put("c", TWO);

        assertTrue(map.findAny((key, val) -> key.equals("a") && val.equals(ONE)));
        assertFalse(clone.findAny((key, val) -> key.equals("a")));
        assertFalse(map.findAny((key, val) -> key.equals("b")));
        assertTrue(clone.findAny((key, val) -> key.equals("b") && val.equals(ONE)));
        assertTrue(map.findAny((key, val) -> key.equals("c") && val.equals(ONE)));
        assertFalse(map.findAny((key, val) -> key.equals("c") && !val.equals(ONE)));
        assertTrue(clone.findAny((key, val) -> key.equals("c") && val.equals(TWO)));
        assertFalse(clone.findAny((key, val) -> key.equals("c") && !val.equals(TWO)));

        ctx.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testKnownArray(TestContext ctx) throws MethodExecutor.VmException {
        var arr = KnownArray.withLength(2);
        arr.arraySet(0, StackEntry.known(1));
        assertEq(arr, arr.copy());
        assertEq(arr.hashCode(), arr.copy().hashCode());

        ctx.complete();
    }

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void vmHash(TestContext ctx) throws MethodExecutor.VmException {
        var vm = new VirtualMachine(new ClientClassLoader(), new VirtualMachine.VmConfig() { });
        var vmHash = vm.hashCode();
        var copy = vm.copy();
        assertEq(vm, copy);
        assertEq(vm.hashCode(), copy.hashCode());

        copy.addMethodToStack(Type.getInternalName(CowAndHashcode.class), "lilMethod", "()I");
        assertEq(vmHash, vm.hashCode(), "Hash shouldn't change after editing copy");
        copy.runToCompletion();
        assertEq(vmHash, vm.hashCode(), "Hash shouldn't change after editing copy");
        ctx.complete();
    }

    private static int lilMethod() {
        var list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            list.add("yeet");
        }
        return list.size();
    }
}
