package com.ldtteam.perviaminvenire.pathfinding;

import com.ldtteam.perviaminvenire.api.pathfinding.AbstractPathJob;
import com.ldtteam.perviaminvenire.api.pathfinding.CalculationNode;
import com.ldtteam.perviaminvenire.util.CalculationNodeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Job that handles moving to a location.
 */
public class PathJobMoveToLocation extends AbstractPathJob {

    private static final Logger LOGGER = LogManager.getLogger();

    private final int accuracy;
    @NotNull
    private final BlockPos destination;
    private float destinationSlack;

    /**
     * Prepares the PathJob for the path finding system.
     *
     * @param world    world the entity is in.
     * @param start    starting location.
     * @param end      target location.
     * @param accuracy the block range that needs to be fulfilled to be at the destination.
     * @param range    max search range.
     * @param entity   the entity.
     */
    public PathJobMoveToLocation(final Level world, @NotNull final BlockPos start, @NotNull final BlockPos end, final int accuracy, final int range, final LivingEntity entity) {
        super(world, start, end, range, entity);

        this.destination = new BlockPos(end);

        this.accuracy = accuracy;
        this.destinationSlack = accuracy;
    }

    /**
     * Perform the search.
     *
     * @return Path of a path to the given location, a best-effort, or null.
     */
    @Nullable
    @Override
    protected Path search() {
        LOGGER.debug(String.format("Pathfinding from [%d,%d,%d] to [%d,%d,%d]",
                start.getX(), start.getY(), start.getZ(), destination.getX(), destination.getY(), destination.getZ()));

        //  Compute destination slack - if the destination point cannot be stood in
        if (getGroundHeight(null, destination) != destination.getY()) {
            destinationSlack = this.accuracy + CalculationNodeUtils.DESTINATION_SLACK_ADJACENT;
        }

        return super.search();
    }

    @Override
    protected double computeHeuristic(@NotNull final BlockPos pos) {
        return Math.sqrt(destination.distSqr(pos));
    }

    /**
     * Checks if the target has been reached.
     *
     * @param n Node to test.
     * @return true if has been reached.
     */
    @Override
    protected boolean isAtDestination(@NotNull final CalculationNode n) {
        return CalculationNodeUtils.isADestination(n, destinationSlack, destination);
    }

    /**
     * Calculate the distance to the target.
     *
     * @param n Node to test.
     * @return double of the distance.
     */
    @Override
    protected double getNodeResultScore(@NotNull final CalculationNode n) {
        //  For Result Score lower is better
        return destination.distSqr(n.pos);
    }
}
