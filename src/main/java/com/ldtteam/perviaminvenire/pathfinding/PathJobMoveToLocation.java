package com.ldtteam.perviaminvenire.pathfinding;

import com.ldtteam.perviaminvenire.api.pathfinding.AbstractPathJob;
import com.ldtteam.perviaminvenire.api.pathfinding.CalculationNode;
import net.minecraft.core.Vec3i;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Job that handles moving to a location.
 */
public class PathJobMoveToLocation extends AbstractPathJob {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final float DESTINATION_SLACK_NONE = 0.1F;
    // 1^2 + 1^2 + 1^2 + (epsilon of 0.1F)
    private static final float DESTINATION_SLACK_ADJACENT = (float) Math.sqrt(2f);
    private static final double   TIE_BREAKER = 1.001D;
    @NotNull
    private final        BlockPos destination;
    // 0 = exact match
    private              float    destinationSlack = DESTINATION_SLACK_NONE;

    /**
     * The manhattan distance between start and end.
     */
    private final double startEndDist;

    /**
     * Prepares the PathJob for the path finding system.
     *
     * @param world world the entity is in.
     * @param start starting location.
     * @param end   target location.
     * @param range max search range.
     * @param entity the entity.
     */
    public PathJobMoveToLocation(final Level world, @NotNull final BlockPos start, @NotNull final BlockPos end, final int range, final LivingEntity entity)
    {
        super(world, start, end, range, entity);

        this.destination = new BlockPos(end);

        startEndDist = Math.sqrt(start.distSqr(end));
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
        LOGGER.debug(String.format("Pathfinding from [%d,%d,%d] to [%d,%d,%d]",
          start.getX(), start.getY(), start.getZ(), destination.getX(), destination.getY(), destination.getZ()));

        //  Compute destination slack - if the destination point cannot be stood in
        if (getGroundHeight(null, destination) != destination.getY())
        {
            destinationSlack = DESTINATION_SLACK_ADJACENT;
        }

        return super.search();
    }

    @Override
    protected double computeHeuristic(@NotNull final BlockPos pos)
    {
        return Math.sqrt(destination.distSqr(pos));
    }

    /**
     * Checks if the target has been reached.
     *
     * @param n Node to test.
     * @return true if has been reached.
     */
    @Override
    protected boolean  isAtDestination(@NotNull final CalculationNode n)
    {
        if (destinationSlack <= DESTINATION_SLACK_NONE)
        {
            return n.pos.getX() == destination.getX()
                     && n.pos.getY() == destination.getY()
                     && n.pos.getZ() == destination.getZ();
        }

        if (n.pos.getY() == destination.getY() - 1)
        {
            return destination.closerThan(new Vec3i(n.pos.getX(), destination.getY(), n.pos.getZ()), DESTINATION_SLACK_ADJACENT);
        }

        return destination.closerThan(n.pos, DESTINATION_SLACK_ADJACENT);
    }

    /**
     * Calculate the distance to the target.
     *
     * @param n Node to test.
     * @return double of the distance.
     */
    @Override
    protected double getNodeResultScore(@NotNull final CalculationNode n)
    {
        //  For Result Score lower is better
        return destination.distSqr(n.pos);
    }
}
