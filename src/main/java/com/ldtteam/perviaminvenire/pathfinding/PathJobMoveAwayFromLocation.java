package com.ldtteam.perviaminvenire.pathfinding;

import com.ldtteam.perviaminvenire.api.pathfinding.AbstractPathJob;
import com.ldtteam.perviaminvenire.api.pathfinding.CalculationNode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Job that handles moving away from something.
 */
public class PathJobMoveAwayFromLocation extends AbstractPathJob
{
    private static final Logger LOGGER = LogManager.getLogger();

    @NotNull
    protected final BlockPos avoid;
    protected final int      avoidDistance;
    private final int accuracy;

    /**
     * Prepares the PathJob for the path finding system.
     *
     * @param world         world the entity is in.
     * @param start         starting location.
     * @param avoid         location to avoid.
     * @param avoidDistance how far to move away.
     * @param accuracy      the block range that needs to be fulfilled to be at the destination.
     * @param range         max range to search.
     * @param entity the entity.
     */
    public PathJobMoveAwayFromLocation(final Level world, @NotNull final BlockPos start, @NotNull final BlockPos avoid, final int avoidDistance, final int accuracy, final int range, final LivingEntity entity)
    {
        super(world, start, avoid, range, entity);

        this.avoid = new BlockPos(avoid);
        this.avoidDistance = avoidDistance;

        this.accuracy = accuracy;
    }

    /**
     * Perform the search.
     *
     * @return Path of a path to the given location, a best-effort, or null.
     */
    @Nullable
    @Override
    protected Path search()
    {
        LOGGER.debug(String.format("Pathfinding from [%d,%d,%d] away from [%d,%d,%d]",
          start.getX(), start.getY(), start.getZ(), avoid.getX(), avoid.getY(), avoid.getZ()));

        return super.search();
    }

    /**
     * For MoveAwayFromLocation we want our heuristic to weight.
     *
     * @param pos Position to compute heuristic from.
     * @return heuristic as a double - Manhatten Distance with tie-breaker.
     */
    @Override
    protected double computeHeuristic(@NotNull final BlockPos pos)
    {
        return -avoid.distSqr(pos);
    }

    /**
     * Checks if the destination has been reached.
     * Meaning that the avoid distance has been reached.
     *
     * @param n Node to test.
     * @return true if so.
     */
    @Override
    protected boolean isAtDestination(@NotNull final CalculationNode n)
    {
        return Math.sqrt(avoid.distSqr(n.pos)) > (avoidDistance - accuracy);
    }

    /**
     * Calculate the distance to the target.
     *
     * @param n Node to test.
     * @return double amount.
     */
    @Override
    protected double getNodeResultScore(@NotNull final CalculationNode n)
    {
        return -avoid.distSqr(n.pos);
    }
}
