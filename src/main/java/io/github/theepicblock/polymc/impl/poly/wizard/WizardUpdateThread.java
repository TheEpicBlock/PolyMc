package io.github.theepicblock.polymc.impl.poly.wizard;

import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.api.wizard.UpdateInfo;
import io.github.theepicblock.polymc.impl.mixin.WizardTickerDuck;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.thread.ReentrantThreadExecutor;

import java.util.Set;

public class WizardUpdateThread extends ReentrantThreadExecutor<Runnable> {
    public static WizardUpdateThread MAIN_THREAD = null;
    private static final int MILLIS_PER_TICK = 1000/40;

    private final MinecraftServer server;
    public final Set<ServerWorld> worlds = new ObjectArraySet<>();
    private final Thread myThread = new Thread(this::runTickLoop);
    private boolean shouldStop = false;
    private volatile int tickTime = 0;
    private volatile long tickStart = System.nanoTime(); // The time in milliseconds of when this tick started, used to calculate tick delta

    public WizardUpdateThread(MinecraftServer server) {
        super("PolyMc wizard updater");
        this.server = server;
    }

    public static void registerEvents() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            var wizardTicker = new WizardUpdateThread(server);
            wizardTicker.start();
            MAIN_THREAD = wizardTicker;
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            if (MAIN_THREAD == null) return;
            MAIN_THREAD.stop();
        });

        ServerWorldEvents.LOAD.register((server, world) -> {
            if (MAIN_THREAD == null) return;
            MAIN_THREAD.execute(() -> MAIN_THREAD.worlds.add(world));
        });

        ServerWorldEvents.UNLOAD.register((server, world) -> {
            if (MAIN_THREAD == null) return;
            MAIN_THREAD.execute(() -> MAIN_THREAD.worlds.remove(world));
        });

        ServerTickEvents.START_SERVER_TICK.register(server -> {
            MAIN_THREAD.tickTime = server.getTicks();
            MAIN_THREAD.tickStart = System.nanoTime();
        });
    }

    public void start() {
        // Ran on the main thread
        server.getWorlds().forEach(this.worlds::add);
        myThread.start();
        myThread.setDaemon(true);
        myThread.setName("PolyMc wizard updater");
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
                var tickers = ((WizardTickerDuck)world).polymc$getTickers();
                tickers.forEach((polyMap, wizardsPerPos) -> {
                    wizardsPerPos.forEach((pos, wizards) -> {
                        var playerView = new CachedPolyMapFilteredPlayerView(PolyMapFilteredPlayerView.getAll(world, pos), polyMap);
                        var updateInfo = new UpdateInfoImpl(this.tickTime, getTickDelta());
                        wizards.forEach(wizard -> wizard.update(playerView, updateInfo));
                    });
                });
            });

            var tickStartMilli = this.tickStart / 1000000;
            try {
                Thread.sleep(tickStartMilli + MILLIS_PER_TICK); // TODO more intelligent scheduling
            } catch (InterruptedException ignored) {}
        }
    }

    private float getTickDelta() {
        return (System.nanoTime()-this.tickStart) / 1000000000f;
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

    public class UpdateInfoImpl implements UpdateInfo {
        private final int tick;
        private final float tickDelta;

        public UpdateInfoImpl(int tick, float tickDelta) {
            this.tick = tick;
            this.tickDelta = tickDelta;
        }

        @Override
        public int getTick() {
            return 0;
        }

        @Override
        public float getTickDelta() {
            return 0;
        }
    }
}
