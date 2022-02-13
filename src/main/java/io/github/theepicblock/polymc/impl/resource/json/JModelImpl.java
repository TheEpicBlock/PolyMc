package io.github.theepicblock.polymc.impl.resource.json;

import com.google.gson.annotations.SerializedName;
import io.github.theepicblock.polymc.api.resource.json.*;

import java.util.List;
import java.util.Map;

public class JModelImpl {
    /**
     * If there's a credit field, keep it. We don't want to erase attribution
     */
    @SerializedName(value = "credit", alternate = "__comment")
    private String credit;

    public String parent;
    public JGuiLight gui_light;
    public Map<String, String> textures;
    public List<JElement> elements;
    public Map<JModelDisplayType,JModelDisplay> display;
    public List<JModelOverride> overrides;

    /**
     * Ensures that the {@link #overrides} list is properly sorted so that the lowest priority go on top.
     */
    public void sortOverrides() {
        if (overrides == null) return;
        overrides.sort((o1, o2) -> {
            if (o1.predicates().size() > 0 && o2.predicates().size() > 0) {
                double i1 = o1.predicates().values().iterator().next();
                double i2 = o2.predicates().values().iterator().next();
                return (int)(i1 - i2);
            }
            return 0;
        });
    }
}
