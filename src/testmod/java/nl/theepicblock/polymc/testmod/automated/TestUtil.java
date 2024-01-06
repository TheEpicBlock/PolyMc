package nl.theepicblock.polymc.testmod.automated;

import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.api.PolyMap;
import net.minecraft.test.GameTestException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Predicate;

public class TestUtil {
    public static PolyMap getMap() {
        return PolyMc.getMainMap();
    }

    @Contract("null, _ -> fail")
    public static void assertNonNull(@Nullable Object o, String message) {
        if (o == null) {
            var line = Thread.currentThread().getStackTrace()[2].getLineNumber();
            throw new GameTestException("L"+line+": "+message);
        }
    }

    public static void assertTrue(boolean a) {
        if (!a) {
            var line = Thread.currentThread().getStackTrace()[2].getLineNumber();
            throw new GameTestException("L"+line+" Assertion failed");
        }
    }

    public static void assertTrue(boolean a, String message) {
        if (!a) {
            var line = Thread.currentThread().getStackTrace()[2].getLineNumber();
            throw new GameTestException("L"+line+": "+message);
        }
    }

    public static void assertFalse(boolean a) {
        if (a) {
            var line = Thread.currentThread().getStackTrace()[2].getLineNumber();
            throw new GameTestException("L"+line+" Assertion failed");
        }
    }

    public static void assertFalse(boolean a, String message) {
        if (a) {
            var line = Thread.currentThread().getStackTrace()[2].getLineNumber();
            throw new GameTestException("L"+line+": "+message);
        }
    }

    public static void assertEq(Object value, Object expected) {
        if (!Objects.equals(value, expected)) {
            var line = Thread.currentThread().getStackTrace()[2].getLineNumber();
            throw new GameTestException("L"+line+" Assertion failed: "+value+" != "+expected);
        }
    }

    public static void assertEq(Object value, Object expected, String message) {
        if (!Objects.equals(value, expected)) {
            var line = Thread.currentThread().getStackTrace()[2].getLineNumber();
            throw new GameTestException("L"+line+": "+value+" != "+expected+" "+message);
        }
    }

    public static void assertDifferent(Object a, Object b) {
        if (Objects.equals(a, b)) {
            var line = Thread.currentThread().getStackTrace()[2].getLineNumber();
            throw new GameTestException("L"+line+" Assertion failed: "+a+" = "+b);
        }
    }

    public static void assertDifferent(Object a, Object b, String message) {
        if (Objects.equals(a, b)) {
            var line = Thread.currentThread().getStackTrace()[2].getLineNumber();
            throw new GameTestException("L"+line+": "+a+" = "+b+" "+message);
        }
    }

    public static void assertThrows(Predicate<Throwable> check, String message, Runnable runnable) {
        var line = Thread.currentThread().getStackTrace()[2].getLineNumber();

        try {
            runnable.run();
        } catch (Throwable t) {
            if (!check.test(t)) {
                throw new GameTestException("L"+line+" Method threw invalid exception: "+t+" ("+message+")");
            }
            return;
        }

        throw new GameTestException("L"+line+" Method didn't throw exception: "+message);
    }
}
