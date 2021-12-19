package io.github.theepicblock.polymc.impl.resource.json;

import io.github.theepicblock.polymc.api.resource.json.JGuiLight;
import io.github.theepicblock.polymc.api.resource.json.JModelDisplay;
import io.github.theepicblock.polymc.api.resource.json.JModelDisplayType;
import io.github.theepicblock.polymc.api.resource.json.JModelOverride;

import java.util.List;
import java.util.Map;

public class JModelImpl {
    public String parent;
    public JGuiLight gui_light;
    public Map<String, String> textures;
    public Map<JModelDisplayType,JModelDisplay> display;
    public List<JModelOverride> overrides;
}
