package nl.theepicblock.polymc.testmod.automated;

import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.api.PolyMap;
import net.minecraft.test.GameTestException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;


public class TestUtil {
    public static PolyMap getMap() {
        return PolyMc.getMainMap();
    }

    @Contract("null, _ -> fail")
    public static void assertNonNull(@Nullable Object o, String message) {
        if (o == null) {
            throw new GameTestException(message);
        }
    }

    public static void assertEq(Object a, Object b) {
        if (!Objects.equals(a, b)) {
            throw new GameTestException("Assertion failed: "+a+" != "+b);
        }
    }

    public static void assertEq(Object a, Object b, String message) {
        if (!Objects.equals(a, b)) {
            throw new GameTestException("Assertion failed: "+a+" != "+b+" "+message);
        }
    }
}
