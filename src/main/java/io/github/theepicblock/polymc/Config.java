package io.github.theepicblock.polymc;

import java.util.List;
import java.util.Map;

public class Config {
    private List<String> disabledMixins;

    public boolean isMixinDisabled(String mixin) {
        if (disabledMixins == null) return false;
        return disabledMixins.contains(mixin);
    }
}
