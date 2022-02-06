package io.github.theepicblock.polymc.stubs;

import io.github.theepicblock.polymc.stubs.loader.StubMappingInfo;

/**
 * @see net.minecraft.client.gui.screen.Screen
 */
@StubMappingInfo(intermediary = "net.minecraft.class_437")
public class ScreenStub {
    @StubMappingInfo(intermediary = "method_25442")
    public static boolean hasShiftDown() {
        return true;
    }

    @StubMappingInfo(intermediary = "method_25443")
    public static boolean hasAltDown() {
        return true;
    }
}
