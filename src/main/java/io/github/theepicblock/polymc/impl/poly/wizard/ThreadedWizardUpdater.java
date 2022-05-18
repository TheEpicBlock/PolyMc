package io.github.theepicblock.polymc.impl.poly.wizard;

import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.api.wizard.PacketConsumer;
import io.github.theepicblock.polymc.api.wizard.UpdateInfo;
import io.github.theepicblock.polymc.impl.mixin.WizardTickerDuck;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.thread.ReentrantThreadExecutor;
import org.jetbrains.annotations.ApiStatus;

import java.lang.annotation.*;
import java.util.Set;

public class ThreadedWizardUpdater extends ReentrantThreadExecutor<Runnable> {
    public static ThreadedWizardUpdater MAIN = null;
    private static final int MILLIS_PER_TICK = 1000/60;

    private final MinecraftServer server;
    public final Set<ServerWorld> worlds = new ObjectArraySet<>();
    private final Thread myThread = new Thread(this::runTickLoop);
    private boolean shouldStop = false;
    private volatile int tickTime = 0;
    private volatile long tickStart = System.nanoTime(); // The time in milliseconds of when this tick started, used to calculate tick delta

    public ThreadedWizardUpdater(MinecraftServer server) {
        super("PolyMc wizard updater");
        this.server = server;
    }

    public static void registerEvents() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            var wizardTicker = new ThreadedWizardUpdater(server);
            wizardTicker.start();
            MAIN = wizardTicker;
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            if (MAIN == null) return;
            MAIN.stop();
        });

        ServerWorldEvents.LOAD.register((server, world) -> {
            if (MAIN == null) return;
            MAIN.execute(() -> MAIN.worlds.add(world));
        });

        ServerWorldEvents.UNLOAD.register((server, world) -> {
            if (MAIN == null) return;
            MAIN.execute(() -> MAIN.worlds.remove(world));
        });

        ServerTickEvents.START_SERVER_TICK.register(server -> {
            if (MAIN == null) return;
            MAIN.tickTime = server.getTicks();
            MAIN.tickStart = System.nanoTime();
        });

        // This calls the regular on tick method (not the update method). This is done on the main thread like normal
        ServerTickEvents.END_WORLD_TICK.register(world -> ((WizardTickerDuck)world).polymc$getBlockTickers()
                .forEach((polyMap, wizardsPerPos) -> {
                    wizardsPerPos.forEach((pos, wizards) -> {
                        var playerView = new CachedPolyMapFilteredPlayerView(PolyMapFilteredPlayerView.getAll(world, pos), polyMap);
                        wizards.forEach(wizard -> {
                            wizard.onTick(playerView);
                        });
                        playerView.sendBatched();
                    });
                }));
    }

    public void start() {
        // Ran on the main thread
        server.getWorlds().forEach(this.worlds::add);
        myThread.setDaemon(true);
        myThread.setName("PolyMc wizard updater");
        myThread.start();
    }

    public void stop() {
        // Ran from the main thread
        this.execute(() -> {
            PolyMc.LOGGER.info("Stopping wizard updating thread");
            this.shouldStop = true;
        });
    }

    @SuppressWarnings("BusyWait")
    public void runTickLoop() {
        // This is the entrypoint into the thread
        PolyMc.LOGGER.info("Started wizard updating thread");
        while (true) {
            this.runTasks();
            if (shouldStop) {
                return;
            }

            this.worlds.forEach(world -> {

                var entityUpdateInfo = new UpdateInfoImpl(this.tickTime, getTickDelta());
                ((WizardTickerDuck)world).polymc$getEntityTickers().forEach((polyMap, wizards) -> {
                    wizards.forEach(wizard -> {
                        var playerView = new CachedPolyMapFilteredPlayerView(PolyMapFilteredPlayerView.getAll(world, new ChunkPos((int)wizard.getPosition().x >> 4, (int)wizard.getPosition().z >> 4)), polyMap); // FIXME this is *not* threadsafe
                        wizard.update(playerView, entityUpdateInfo);
                        playerView.sendBatched();
                    });
                });
                ((WizardTickerDuck)world).polymc$getBlockTickers().forEach((polyMap, wizardsPerPos) -> {
                    wizardsPerPos.forEach((pos, wizards) -> {
                        var playerView = new CachedPolyMapFilteredPlayerView(PolyMapFilteredPlayerView.getAll(world, pos), polyMap); // FIXME this is *not* threadsafe
                        var updateInfo = new UpdateInfoImpl(this.tickTime, getTickDelta());
                        wizards.forEach(wizard -> wizard.update(playerView, updateInfo));
                        playerView.sendBatched();
                    });
                });
            });

            try {
                Thread.sleep(MILLIS_PER_TICK); // TODO more intelligent scheduling
            } catch (InterruptedException ignored) {}
        }
    }

    private float getTickDelta() {
        return (System.nanoTime()-this.tickStart) / (1000000000f/20f);
    }

    @Override
    protected Runnable createTask(Runnable runnable) {
        return runnable;
    }

    @Override
    protected boolean canExecute(Runnable task) {
        return true;
    }

    @Override
    protected Thread getThread() {
        return myThread;
    }

    public record UpdateInfoImpl(int tick, float tickDelta) implements UpdateInfo {

        @Override
        public int getTick() {
            return tick;
        }

        @Override
        public float getTickDelta() {
            return tickDelta;
        }
    }

    /**
     * Marks a method to be safe to call from a threaded context such as {@link io.github.theepicblock.polymc.api.wizard.Wizard#update(PacketConsumer, UpdateInfo)}
     */
    @Documented
    @ApiStatus.Experimental
    @Retention(RetentionPolicy.CLASS)
    @Target({ElementType.METHOD})
    public @interface Safe {

    }

    /**
     * Marks a method to be kind of safe to call from a threaded context such as {@link io.github.theepicblock.polymc.api.wizard.Wizard#update(PacketConsumer, UpdateInfo)}
     * Usually the method itself is safe to call, but there are some other things that aren't safe. These might be explained in the javadoc.
     */
    @Documented
    @ApiStatus.Experimental
    @Retention(RetentionPolicy.CLASS)
    @Target({ElementType.METHOD})
    public @interface KindaSafe {

    }

    /**
     * Marks a method to be unsafe to call from a threaded context such as {@link io.github.theepicblock.polymc.api.wizard.Wizard#update(PacketConsumer, UpdateInfo)}
     */
    @Documented
    @ApiStatus.Experimental
    @Retention(RetentionPolicy.CLASS)
    @Target({ElementType.METHOD})
    public @interface Unsafe {

    }
}
