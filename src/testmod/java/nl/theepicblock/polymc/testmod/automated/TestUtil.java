package nl.theepicblock.polymc.testmod.automated;

import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.api.PolyMap;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.test.GameTestException;
import net.minecraft.test.TestContext;
import net.minecraft.test.TestFunction;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;


public class TestUtil {
    public static PolyMap getMap() {
        return PolyMc.getMainMap();
    }

    @Contract("null, _ -> fail")
    public static void assertNonNull(@Nullable Object o, String message) {
        if (o == null) {
            var line = Thread.currentThread().getStackTrace()[2].getLineNumber();
            throw new GameTestException("L"+line+" "+message);
        }
    }

    public static void assertTrue(boolean a) {
        if (!a) {
            var line = Thread.currentThread().getStackTrace()[2].getLineNumber();
            throw new GameTestException("L"+line+" Assertion failed");
        }
    }

    public static void assertFalse(boolean a) {
        if (a) {
            var line = Thread.currentThread().getStackTrace()[2].getLineNumber();
            throw new GameTestException("L"+line+" Assertion failed");
        }
    }

    public static void assertEq(Object a, Object b) {
        if (!Objects.equals(a, b)) {
            var line = Thread.currentThread().getStackTrace()[2].getLineNumber();
            throw new GameTestException("L"+line+" Assertion failed: "+a+" != "+b);
        }
    }

    public static void assertEq(Object a, Object b, String message) {
        if (!Objects.equals(a, b)) {
            var line = Thread.currentThread().getStackTrace()[2].getLineNumber();
            throw new GameTestException("L"+line+" Assertion failed: "+a+" != "+b+" "+message);
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
            throw new GameTestException("L"+line+" Assertion failed: "+a+" = "+b+" "+message);
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

    public static TestBuilder testBuilder() {
        return new TestBuilder();
    }

    public static class TestBuilder {
        private String batch = "defaultBatch";
        private String name;
        private String templateName = FabricGameTest.EMPTY_STRUCTURE;
        private int tickLimit = 100;
        private long duration = 0;
        private boolean required = true;
        private Consumer<TestContext> func;

        public TestBuilder batch(String batch) {
            this.batch = batch;
            return this;
        }

        public TestBuilder name(String name) {
            try {
                var caller = Thread.currentThread().getStackTrace()[2].getClassName();
                var callerClass = Class.forName(caller);
                // Idk why this is all lowercase, but I'm just following MC's convention
                this.name = callerClass.getSimpleName().toLowerCase(Locale.ROOT)+"."+name.toLowerCase(Locale.ROOT);
                return this;
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        public TestBuilder templateName(String templateName) {
            this.templateName = templateName;
            return this;
        }

        public TestBuilder tickLimit(int tickLimit) {
            this.tickLimit = tickLimit;
            return this;
        }

        public TestBuilder duration(long duration) {
            this.duration = duration;
            return this;
        }

        public TestBuilder required(boolean required) {
            this.required = required;
            return this;
        }

        public TestBuilder func(Consumer<TestContext> func) {
            this.func = func;
            return this;
        }

        public TestFunction build() {
            return new TestFunction(
                    batch,
                    name,
                    templateName,
                    tickLimit,
                    duration,
                    required,
                    func
            );
        }
    }
}
