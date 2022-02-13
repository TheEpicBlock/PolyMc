package nl.theepicblock.polymc.testmod;

import io.github.theepicblock.polymc.api.PolyMcEntrypoint;
import io.github.theepicblock.polymc.api.PolyRegistry;
import nl.theepicblock.polymc.testmod.poly.TestWizardBlockPoly;

public class EntrypointListener implements PolyMcEntrypoint {
    @Override
    public void registerPolys(PolyRegistry registry) {
        registry.registerBlockPoly(Testmod.TEST_BLOCK_WIZARD, new TestWizardBlockPoly());
    }
}
