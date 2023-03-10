package com.ldtteam.perviaminvenire.api.pathfinding;

import com.ldtteam.perviaminvenire.api.pathfinding.stuckhandling.IStuckHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.pathfinder.Path;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface to indicate that this is an advanced navigator.
 */
public interface IAdvancedPathNavigator {
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
     * @param accuracy        the accuracy to reach.
     * @param range           the range he should move out of.
     * @param speed           the speed to run at.
     * @return the result of the pathing.
     */
    PathResult<?> moveAwayFromXYZ(BlockPos currentPosition, double accuracy, double range, double speed);

    /**
     * Try to move to a certain position.
     *
     * @param x        the x target.
     * @param y        the y target.
     * @param z        the z target.
     * @param accuracy the accuracy to reach.
     * @param speed    the speed to walk.
     * @return the PathResult.
     */
    @Nullable PathResult<?> moveToXYZ(double x, double y, double z, double accuracy, double speed);

    /**
     * Used to path away from a ourEntity.
     *
     * @param target              the ourEntity.
     * @param accuracy            the accuracy to reach.
     * @param range               the distance to move to.
     * @param combatMovementSpeed the speed to run at.
     * @return the result of the pathing.
     */
    @Nullable PathResult<?> moveAwayFromLivingEntity(Entity target, double accuracy, double range, double combatMovementSpeed);

    /**
     * Attempt to move to a specific pos.
     *
     * @param position the position to move to.
     * @param accuracy the accuracy to reach.
     * @param speed    the speed.
     * @return true if successful.
     */
    boolean tryMoveToBlockPos(BlockPos position, double accuracy, double speed);

    /**
     * Used to move a living ourEntity with a speed.
     *
     * @param e        the ourEntity.
     * @param accuracy the accuracy to reach.
     * @param speed    the speed.
     * @return the result.
     */
    @Nullable PathResult<?> moveToLivingEntity(@NotNull Entity e, double accuracy, double speed);

    /**
     * The options used during pathfinding.
     *
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

    /**
     * Forces the navigation to redirect the entity to the given target, potentially replacing the currently followed path with a new one to this target.
     *
     * @param target   The target.
     */
    default void moveTo(BlockPos target) {
        moveToXYZ(target.getX(), target.getY(), target.getZ(), 1.0D, 1.0D);
    }
}
