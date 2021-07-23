package com.ldtteam.perviaminvenire.api.pathfinding;

import com.ldtteam.perviaminvenire.api.IPerViamInvenireApi;
import com.ldtteam.perviaminvenire.api.pathfinding.AbstractPathJob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;

/**
 * Tracker which helps with synchronizing the results of the calculations.
 * This is only used during debugging, normal calculation results are not synced.
 */
public interface ICalculationResultTracker
{

    /**
     * Static accessor for the tracker.
     *
     * @return The tracker.
     */
    public static ICalculationResultTracker getInstance()
    {
        return IPerViamInvenireApi.getInstance().getResultTracker();
    }

    /**
     * Triggered when an entity leaves the world.
     * This handles the case where a player logs-off/switches-dimensions or an other entity is removed.
     *
     * Tracking should stop when this is invoked.
     *
     * @param entity The entity that left the world it is in.
     */
    void onEntityLeaveWorld(Entity entity);

    /**
     * Invoked to start tracking a given entity by a given player.
     *
     * @param playerEntity The player that should start tracking.
     * @param entity The entity that is being tracked.
     */
    void startTracking(Player playerEntity, Entity entity);

    /**
     * Invoked to stop tracking a given entity by a given player.
     *
     * @param playerEntity The player that should stop tracking.
     * @param entity The entity that is being tracked.
     */
    void stopTracking(Player playerEntity, Entity entity);

    /**
     * Indicates to start exporting the pathing results of a given entity.
     *
     * @param entity The entity to start exporting.
     */
    void startExporting(Entity entity);

    /**
     * Indicates to stop exporting the pathing results of a given entity.
     *
     * @param entity The entity to stop exporting.
     */
    void stopExporting(Entity entity);

    /**
     * Invoked when a particular job completes calculation.
     * Triggers the synchronization if needed.
     *
     * @param job The job.
     */
    void onCalculationCompleted(AbstractPathJob job);

    /**
     * Returns all players which are tracking a given entity.
     *
     * @param entity The entity to get the tracking players for.
     * @return The tracking players
     */
    Collection<Player> getTrackingPlayers(final Entity entity);
}
