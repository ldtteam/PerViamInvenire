package com.ldtteam.perviaminvenire.pathfinding;

import static com.ldtteam.perviaminvenire.api.util.constants.PathingConstants.DEBUG_VERBOSITY_NONE;

import com.ldtteam.perviaminvenire.api.config.ICommonConfig;
import com.ldtteam.perviaminvenire.api.pathfinding.AbstractPathJob;
import com.ldtteam.perviaminvenire.api.pathfinding.Node;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.LivingEntity;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Job that handles moving away from something.
 */
public class PathJobMoveAwayFromLocation extends AbstractPathJob
{
    private static final Logger LOGGER = LogManager.getLogger();

    private static final double TIE_BREAKER = 1.001D;

    /**
     * Position to run to, in order to avoid something.
     */
    @NotNull
    protected final BlockPos avoid;
    /**
     * Required avoidDistance.
     */
    protected final int      avoidDistance;

    /**
     * Prepares the PathJob for the path finding system.
     *
     * @param world         world the entity is in.
     * @param start         starting location.
     * @param avoid         location to avoid.
     * @param avoidDistance how far to move away.
     * @param range         max range to search.
     * @param entity the entity.
     */
    public PathJobMoveAwayFromLocation(final World world, @NotNull final BlockPos start, @NotNull final BlockPos avoid, final int avoidDistance, final int range, final LivingEntity entity)
    {
        super(world, start, avoid, range, entity);

        this.avoid = new BlockPos(avoid);
        this.avoidDistance = avoidDistance;
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
        return -avoid.distanceSq(pos);
    }

    /**
     * Checks if the destination has been reached.
     * Meaning that the avoid distance has been reached.
     *
     * @param n Node to test.
     * @return true if so.
     */
    @Override
    protected boolean isAtDestination(@NotNull final Node n)
    {
        return Math.sqrt(avoid.distanceSq(n.pos)) > avoidDistance;
    }

    /**
     * Calculate the distance to the target.
     *
     * @param n Node to test.
     * @return double amount.
     */
    @Override
    protected double getNodeResultScore(@NotNull final Node n)
    {
        return -avoid.distanceSq(n.pos);
    }
}
