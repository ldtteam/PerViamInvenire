package com.ldtteam.perviaminvenire.pathfinding;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.ldtteam.perviaminvenire.api.config.ICommonConfig;
import com.ldtteam.perviaminvenire.api.pathfinding.AbstractPathJob;
import org.jetbrains.annotations.NotNull;

import net.minecraft.pathfinding.Path;

/**
 * Static class the handles all the PathFinding.
 */
public final class PathFinding
{
    private static final BlockingQueue<Runnable> jobQueue = new LinkedBlockingDeque<>();
    private static ThreadPoolExecutor executor;
    
    
    /**
     * Creates a new thread pool for pathfinding jobs
     * @return the threadpool executor.
     */
    public static ThreadPoolExecutor getExecutor()
    {
        if (executor == null)
        {
            executor = new ThreadPoolExecutor(1, ICommonConfig.getInstance().getPathFindingThreadingCount(), 10, TimeUnit.SECONDS, jobQueue);
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
