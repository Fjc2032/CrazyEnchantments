package com.badbones69.crazyenchantments.paper.tasks.processors;

import com.badbones69.crazyenchantments.paper.CrazyEnchantments;
import com.badbones69.crazyenchantments.paper.scheduler.FoliaRunnable;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class PoolProcessor {

    private final CrazyEnchantments plugin =  JavaPlugin.getPlugin(CrazyEnchantments.class);

    private final Server server = this.plugin.getServer();

    private ThreadPoolExecutor executor = null;

    private final int maxQueueSize = 10000;

    private ScheduledTask taskId;

    public PoolProcessor() {
        start();
    }

    /**
     * Adds the task into the thread pool to be processed.
     * @param process The {@link Runnable} to process.
     */
    public void add(final Runnable process) {
        this.executor.submit(process);
    }

    /**
     * Creates the thread pool used to process tasks.
     */
    public void start() {
        if (executor == null) this.executor = new ThreadPoolExecutor(1, 10, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(this.maxQueueSize));

        this.executor.allowCoreThreadTimeOut(true);

        resizeChecker();
    }

    /**
     * Terminates the thread pool.
     */
    public void stop() {
        this.taskId.cancel();
        this.executor.shutdown();
        this.executor = null;
    }

    /**
     * Used to increase the default workers in the thread pool.
     * This should ensure that with a higher player count, that all tasks are processed.
     */
    private void resizeChecker() {
        this.taskId = new FoliaRunnable(this.server.getAsyncScheduler(), TimeUnit.SECONDS) { //todo() fusion api
            @Override
            public void run() {
                if ((executor.getQueue().size() / executor.getCorePoolSize() > maxQueueSize / 5) && !(executor.getMaximumPoolSize() <= executor.getCorePoolSize() + 1)) {
                    executor.setCorePoolSize(executor.getCorePoolSize() + 1);
                }
            }
        }.runAtFixedRate(this.plugin, 20, 100);
    }
}