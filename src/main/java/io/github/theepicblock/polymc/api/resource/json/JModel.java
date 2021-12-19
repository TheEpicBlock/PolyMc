package io.github.theepicblock.polymc.api.resource.json;

import io.github.theepicblock.polymc.api.resource.ModdedResources;
import io.github.theepicblock.polymc.api.resource.PolyMcAsset;
import io.github.theepicblock.polymc.api.resource.PolyMcResourcePack;
import io.github.theepicblock.polymc.impl.Util;
import net.minecraft.util.Identifier;

import java.util.List;

public interface JModel extends PolyMcAsset {
    @Override
    default void importRequirements(ModdedResources from, PolyMcResourcePack to) {
        var parent = Identifier.tryParse(this.getParent());
        if (parent != null && !Util.isVanilla(parent)) {
            var parentModel = from.getModel(parent.getNamespace(), parent.getPath());
            if (parentModel != null) {
                to.setModel(parent.getNamespace(), parent.getPath(), parentModel);
                to.importRequirements(from, parentModel);
            }
        }

        for (var override : this.getOverridesReadOnly()) {
            var id = Identifier.tryParse(override.model());
            if (id != null && !Util.isVanilla(id)) {
                var model = from.getModel(id.getNamespace(), id.getPath());
                if (model != null) {
                    to.setModel(id.getNamespace(), id.getPath(), model);
                    to.importRequirements(from, model);
                }
            }
        }
    }

    String getParent();
    void setParent(String v);

    JGuiLight getGuiLight();
    void setGuiLight(JGuiLight v);

    String getTexture(String textureName);
    void setTexture(String textureName, String texture);

    JModelDisplay getDisplay(JModelDisplayType position);
    void setDisplay(JModelDisplayType position, JModelDisplay display);

    List<JModelOverride> getOverridesReadOnly();
    List<JModelOverride> getOverrides();
}
