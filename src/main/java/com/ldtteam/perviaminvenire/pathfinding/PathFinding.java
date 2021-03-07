package com.ldtteam.perviaminvenire.pathfinding;

import java.util.concurrent.*;

import com.ldtteam.perviaminvenire.PerViamInvenire;
import com.ldtteam.perviaminvenire.api.config.ICommonConfig;
import com.ldtteam.perviaminvenire.api.pathfinding.AbstractPathJob;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import net.minecraft.pathfinding.Path;

/**
 * Static class the handles all the PathFinding.
 */
public final class PathFinding
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final BlockingQueue<Runnable> jobQueue = new LinkedBlockingDeque<>();
    private static ThreadPoolExecutor executor;

    /**
     * PVI specific thread factory.
     */
    public static class PVIThreadFactory implements ThreadFactory
    {
        private static final Logger LOGGER = LogManager.getLogger();

        private final ClassLoader classLoader;

        /**
         * Ongoing thread IDs.
         */
        private static int id;

        public PVIThreadFactory(final ClassLoader classLoader) {this.classLoader = classLoader;}

        @Override
        public Thread newThread(@NotNull final Runnable runnable)
        {
            final Thread thread = new Thread(runnable, "PVI Pathfinding Worker #" + (id++));
            thread.setDaemon(true);

            thread.setContextClassLoader(classLoader);
            thread.setUncaughtExceptionHandler((thread1, throwable) -> LOGGER.error("PVI Pathfinding Thread errored! ", throwable));
            return thread;
        }
    }

    /**
     * Creates a new thread pool for pathfinding jobs
     * @return the threadpool executor.
     */
    public static ThreadPoolExecutor getExecutor()
    {
        if (executor == null)
        {
            executor = new ThreadPoolExecutor(1, ICommonConfig.getInstance().getPathFindingThreadingCount(), 10, TimeUnit.SECONDS, jobQueue, new PVIThreadFactory(PerViamInvenire.class.getClassLoader()));
        }
        return executor;
    }
    
    /**
     * Stops all running threads in this thread pool
     */
    public static void shutdown()
    {
        getExecutor().shutdownNow();
        jobQueue.clear();
        executor = null;
    }
    
    private PathFinding()
    {
        //Hides default constructor.
    }

    /**
     * Add a job to the queue for processing.
     *
     * @param job PathJob
     * @return a Future containing the Path
     */
    public static Future<Path> enqueue(@NotNull final AbstractPathJob job)
    {
        return getExecutor().submit(job);
    }
}
