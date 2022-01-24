package io.github.theepicblock.polymc.impl.resource.json;

import io.github.theepicblock.polymc.api.resource.json.JGuiLight;
import io.github.theepicblock.polymc.api.resource.json.JModelDisplay;
import io.github.theepicblock.polymc.api.resource.json.JModelDisplayType;
import io.github.theepicblock.polymc.api.resource.json.JModelOverride;

import java.util.List;
import java.util.Map;

public class JModelImpl {
    /**
     * If there's a credit field, keep it. We don't want to erase attribution
     */
    private String credit;

    public String parent;
    public JGuiLight gui_light;
    public Map<String, String> textures;
    public Map<JModelDisplayType,JModelDisplay> display;
    public List<JModelOverride> overrides;
}
