package io.github.theepicblock.polymc.api.resource.json;

import io.github.theepicblock.polymc.api.resource.ModdedResources;
import io.github.theepicblock.polymc.api.resource.PolyMcAsset;
import io.github.theepicblock.polymc.api.resource.PolyMcResourcePack;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.misc.logging.SimpleLogger;
import io.github.theepicblock.polymc.impl.resource.json.JModelWrapper;

import java.util.List;
import java.util.Map;

public interface JModel extends PolyMcAsset {
    @Override
    default void importRequirements(ModdedResources from, PolyMcResourcePack to, SimpleLogger logger) {
        var parent = Util.parseId(this.getParent());
        if (parent != null && !Util.isVanilla(parent) && to.getModel(parent.getNamespace(), parent.getPath()) == null) {
            var parentModel = from.getModel(parent.getNamespace(), parent.getPath());
            if (parentModel != null) {
                to.setModel(parent.getNamespace(), parent.getPath(), parentModel);
                to.importRequirements(from, parentModel, logger);
            } else {
                logger.error("Couldn't find parent model %s".formatted(this.getParent()));
            }
        }

        for (var textureId : this.getTextures().values()) {
            var id = Util.parseId(textureId);
            if (id != null && !Util.isVanilla(id) && to.getTexture(id.getNamespace(), id.getPath()) == null) {
                var texture = from.getTexture(id.getNamespace(), id.getPath());
                if (texture != null) {
                    to.setTexture(id.getNamespace(), id.getPath(), texture);
                    to.importRequirements(from, texture, logger);
                } else {
                    logger.error("Couldn't find texture model %s".formatted(textureId));
                }
            }
        }

        for (var override : this.getOverridesReadOnly()) {
            var id = Util.parseId(override.model());
            if (id != null && !Util.isVanilla(id) && to.getModel(id.getNamespace(), id.getPath()) == null) {
                var model = from.getModel(id.getNamespace(), id.getPath());
                if (model != null) {
                    to.setModel(id.getNamespace(), id.getPath(), model);
                    to.importRequirements(from, model, logger);
                } else {
                    logger.error("Couldn't find override model %s".formatted(override));
                }
            }
        }
    }

    String getParent();
    void setParent(String v);

    JGuiLight getGuiLight();
    void setGuiLight(JGuiLight v);

    Map<String, String> getTextures();

    List<JElement> getElements();

    JModelDisplay getDisplay(JModelDisplayType position);
    void setDisplay(JModelDisplayType position, JModelDisplay display);

    List<JModelOverride> getOverridesReadOnly();
    List<JModelOverride> getOverrides();

    static JModel create() {
        return new JModelWrapper();
    }
}
