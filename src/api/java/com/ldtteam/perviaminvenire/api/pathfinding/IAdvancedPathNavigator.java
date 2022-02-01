package com.ldtteam.perviaminvenire.api.pathfinding;

import com.ldtteam.perviaminvenire.api.pathfinding.stuckhandling.IStuckHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.pathfinder.Path;
import org.jetbrains.annotations.Nullable;

/**
 * Interface to indicate that this is an advanced navigator.
 */
public interface IAdvancedPathNavigator
{
    /**
     * The destination of the current path finder.
     *
     * @return The current destination.
     */
    @Nullable
    BlockPos getDestination();

    /**
     * Used to path away from a position.
     *
     * @param currentPosition the position to avoid.
     * @param range the range he should move out of.
     * @param speed the speed to run at.
     * @return the result of the pathing.
     */
    PathResult<?> moveAwayFromXYZ(BlockPos currentPosition, double range, double speed);

    /**
     * The options used during pathfinding.
     * @return
     */
    PathingOptions getPathingOptions();

    /**
     * The entity for which the navigation is occurring.
     *
     * @return The entity.
     */
    Mob getOurEntity();

    /**
     * Gets the desired to go position
     *
     * @return desired go to pos
     */
    BlockPos getDesiredPos();

    /**
     * Sets the stuck handler for this navigator
     *
     * @param stuckHandler handler to use
     */
    void setStuckHandler(IStuckHandler stuckHandler);

    /**
     * Stops the current navigator.
     */
    void stopCurrentCalculation();

    /**
     * The current path in the navigator.
     *
     * @return The current path, might be null.
     */
    @Nullable
    Path getCurrentPath();
}
