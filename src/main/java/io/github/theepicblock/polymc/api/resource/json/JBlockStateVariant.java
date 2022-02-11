package io.github.theepicblock.polymc.api.resource.json;

import io.github.theepicblock.polymc.api.resource.AssetWithDependencies;
import io.github.theepicblock.polymc.api.resource.ModdedResources;
import io.github.theepicblock.polymc.api.resource.PolyMcResourcePack;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.misc.logging.SimpleLogger;
import net.minecraft.util.Identifier;

@SuppressWarnings("ClassCanBeRecord") // Records don't work with GSON
public class JBlockStateVariant implements AssetWithDependencies {
    private final String model;
    private final int x;
    private final int y;
    private final boolean uvlock;

    public JBlockStateVariant(String model, int x, int y, boolean uvlock) {
        this.model = model;
        this.x = x;
        this.y = y;
        this.uvlock = uvlock;
    }

    @Override
    public void importRequirements(ModdedResources from, PolyMcResourcePack to, SimpleLogger logger) {
        Identifier id = Util.parseId(this.model());

        if (id != null && !Util.isVanilla(id) && to.getModel(id.getNamespace(), id.getPath()) == null) {
            var model = from.getModel(id.getNamespace(), id.getPath());
            if (model != null) {
                to.setModel(id.getNamespace(), id.getPath(), model);
                to.importRequirements(from, model, logger);
            } else {
                logger.error("Couldn't model %s for blockstate variant".formatted(this.model()));
            }
        }
    }

    public String model() {
        return model;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public boolean uvlock() {
        return uvlock;
    }
}