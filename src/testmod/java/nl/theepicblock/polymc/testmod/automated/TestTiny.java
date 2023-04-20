package nl.theepicblock.polymc.testmod.automated;

import io.github.theepicblock.polymc.impl.generator.asm.Mapping;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

public class TestTiny implements FabricGameTest {
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testIntermediary(TestContext ctx) {
        var mapping = Mapping.intermediaryToObfFromClasspath();

        var identifier = "net.minecraft.class_2960";
        var fapiResolver = FabricLoader.getInstance().getMappingResolver();

        ctx.assertTrue(
            mapping.getClassname(identifier.replace(".", "/"))
            .equals(fapiResolver.unmapClassName("official", fapiResolver.mapClassName("intermediary", identifier))),
            "Failed to map the identifier class");
        ctx.assertTrue(mapping.getClassname("not mapped").equals("not mapped"), "The mappings should ignore values which don't exist");

        ctx.complete();
    }
}
