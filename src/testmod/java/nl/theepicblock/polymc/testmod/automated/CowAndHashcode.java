package nl.theepicblock.polymc.testmod.automated;

import io.github.theepicblock.polymc.impl.generator.asm.ClientClassLoader;
import io.github.theepicblock.polymc.impl.generator.asm.CowCapableMap;
import io.github.theepicblock.polymc.impl.generator.asm.MethodExecutor;
import io.github.theepicblock.polymc.impl.generator.asm.VirtualMachine;
import io.github.theepicblock.polymc.impl.generator.asm.stack.KnownVmObject;
import io.github.theepicblock.polymc.impl.generator.asm.stack.StackEntry;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import org.objectweb.asm.Type;

import java.util.ArrayList;

import static nl.theepicblock.polymc.testmod.automated.TestUtil.assertDifferent;
import static nl.theepicblock.polymc.testmod.automated.TestUtil.assertEq;

public class CowAndHashcode implements FabricGameTest {
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testCowMap(TestContext ctx) throws MethodExecutor.VmException {
        var origin = new CowCapableMap<String>();
        var emptyHashcode = origin.hashCode();
        assertEq(emptyHashcode, new CowCapableMap<String>().hashCode(), "two empty maps should have the same hashcode");

        origin.put("a", StackEntry.known(1));
        origin.put("b", StackEntry.known(1));
        origin.put("c", new KnownVmObject(null, new CowCapableMap<>()));
        origin.put("d", new KnownVmObject(null, new CowCapableMap<>()));
        assertEq(origin.get("a"), StackEntry.known(1));
        assertEq(origin.get("b"), StackEntry.known(1));
        assertEq(origin.get("c"), new KnownVmObject(null, new CowCapableMap<>()));
        assertEq(origin.get("d"), new KnownVmObject(null, new CowCapableMap<>()));

        assertDifferent(origin, new CowCapableMap<>(), "Filled map shouldn't equal empty one");
        assertDifferent(origin.createClone(), new CowCapableMap<>(), "Copied filled map shouldn't equal empty one");

        var originHash = origin.hashCode();
        assertDifferent(originHash, emptyHashcode, "Hashcode should change after something's added");
        origin.put("b", StackEntry.known(1));
        assertEq(originHash, origin.hashCode(), "Hashcode shouldn't change after the exact same value is inserted");

        var copy = origin.createClone();
        // Check if everything's still equal
        assertEq(origin.get("a"), StackEntry.known(1));
        assertEq(origin.get("b"), StackEntry.known(1));
        assertEq(originHash, origin.hashCode(), "Hashcode shouldn't change after copy is made");
        assertEq(copy.get("a"), StackEntry.known(1));
        assertEq(copy.get("b"), StackEntry.known(1));
        assertEq(copy, origin);
        assertEq(copy.hashCode(), origin.hashCode(), "Copy should have same hashcode as original");

        copy.put("a", StackEntry.known(2));
        assertEq(origin.get("a"), StackEntry.known(1), "Copy affecting original");
        assertDifferent(copy, origin);
        assertDifferent(copy.hashCode(), origin.hashCode(), "Hashcode should change after something's changed");

        copy.get("c").setField("yeet", StackEntry.known(1));
        assertEq(origin.get("c"), new KnownVmObject(null, new CowCapableMap<>()), "Copy affecting original");
        assertDifferent(copy, origin);
        assertDifferent(copy.hashCode(), origin.hashCode(), "Hashcode should change after something's changed");

        var copyHash = copy.hashCode();
        origin.put("b", StackEntry.known(2));
        assertEq(copy.get("b"), StackEntry.known(1), "Original affecting copy");
        assertEq(copyHash, copy.hashCode(), "Original affecting copy");

        var copyHash2 = copy.hashCode();
        origin.get("d").setField("yeet", StackEntry.known(1));
        assertEq(copy.get("d"), new KnownVmObject(null, new CowCapableMap<>()), "Original affecting copy");
        assertEq(copyHash2, copy.hashCode(), "Original affecting copy");

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
