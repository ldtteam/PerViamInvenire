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

import java.util.Comparator;
import java.util.Set;

public class PathJobMoveToOneOfLocation extends AbstractPathJob {
    private static final Logger LOGGER = LogManager.getLogger();
    @NotNull
    private final Set<BlockPos> destination;
    private final int accuracy;
    @NotNull
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
    public PathJobMoveToOneOfLocation(final Level world, @NotNull final BlockPos start, @NotNull final Set<BlockPos> end, final int accuracy, final int range, final LivingEntity entity) {
        super(world, start, end.stream().max(Comparator.comparingInt(start::distManhattan)).orElse(start), range, entity);

        this.destination = end;
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
        LOGGER.trace(String.format("Pathfinding from [%d,%d,%d] to any of [%s]",
                start.getX(), start.getY(), start.getZ(), destination));

        return super.search();
    }

    @Override
    protected double computeHeuristic(@NotNull final BlockPos pos) {
        boolean seen = false;
        BlockPos best = null;
        Comparator<BlockPos> comparator = Comparator.comparingDouble(pos::distSqr);
        for (BlockPos blockPos : destination) {
            if (!seen || comparator.compare(blockPos, best) < 0) {
                seen = true;
                best = blockPos;
            }
        }
        return Math.sqrt((seen ? best : pos).distSqr(pos));
    }

    /**
     * Checks if the target has been reached.
     *
     * @param n Node to test.
     * @return true if has been reached.
     */
    @Override
    protected boolean isAtDestination(@NotNull final CalculationNode n) {
        for (BlockPos d : destination) {
            float workingSlack = destinationSlack;

            //  Compute destination slack - if the destination point cannot be stood in
            if (getGroundHeight(null, d) != d.getY()) {
                workingSlack = this.accuracy + CalculationNodeUtils.DESTINATION_SLACK_ADJACENT;
            }

            if (CalculationNodeUtils.isADestination(n, workingSlack, d)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Calculate the distance to the target.
     *
     * @param n Node to test.
     * @return double of the distance.
     */
    @Override
    protected double getNodeResultScore(@NotNull final CalculationNode n) {
        final BlockPos pos = n.pos;
        boolean seen = false;
        BlockPos best = null;
        Comparator<BlockPos> comparator = Comparator.comparingDouble(pos::distSqr);
        for (BlockPos blockPos : destination) {
            if (!seen || comparator.compare(blockPos, best) < 0) {
                seen = true;
                best = blockPos;
            }
        }
        return Math.sqrt((seen ? best : pos).distSqr(pos));
    }
}
