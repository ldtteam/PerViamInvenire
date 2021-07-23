package com.ldtteam.perviaminvenire.pathfinding;

import com.ldtteam.perviaminvenire.api.config.ICommonConfig;
import com.ldtteam.perviaminvenire.api.pathfinding.AbstractPathJob;
import com.ldtteam.perviaminvenire.api.pathfinding.Node;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Set;

import static com.ldtteam.perviaminvenire.api.util.constants.PathingConstants.DEBUG_VERBOSITY_NONE;

public class PathJobMoveToOneOfLocation extends AbstractPathJob
{
    private static final Logger LOGGER = LogManager.getLogger();

    private static final float    DESTINATION_SLACK_NONE = 0.1F;
    // 1^2 + 1^2 + 1^2 + (epsilon of 0.1F)
    private static final float    DESTINATION_SLACK_ADJACENT = (float) Math.sqrt(2f);
    private static final double        TIE_BREAKER = 1.001D;
    @NotNull
    private final        Set<BlockPos> destination;

    /**
     * Prepares the PathJob for the path finding system.
     *
     * @param world world the entity is in.
     * @param start starting location.
     * @param end   target location.
     * @param range max search range.
     * @param entity the entity.
     */
    public PathJobMoveToOneOfLocation(final Level world, @NotNull final BlockPos start, @NotNull final Set<BlockPos> end, final int range, final LivingEntity entity)
    {
        super(world, start, end.stream().max(Comparator.comparingInt(start::distManhattan)).orElse(start), range, entity);

        this.destination = end;
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
        LOGGER.debug(String.format("Pathfinding from [%d,%d,%d] to any of [%s]",
          start.getX(), start.getY(), start.getZ(), destination));

        return super.search();
    }

    @Override
    protected double computeHeuristic(@NotNull final BlockPos pos)
    {
        boolean seen = false;
        BlockPos best = null;
        Comparator<BlockPos> comparator = Comparator.comparingDouble(pos::distSqr);
        for (BlockPos blockPos : destination)
        {
            if (!seen || comparator.compare(blockPos, best) < 0)
            {
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
    protected boolean isAtDestination(@NotNull final Node n)
    {
        for (BlockPos d : destination)
        {
            if (n.pos.getX() == d.getX()
                  && n.pos.getY() == d.getY()
                  && n.pos.getZ() == d.getZ())
            {
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
    protected double getNodeResultScore(@NotNull final Node n)
    {
        final BlockPos pos = n.pos;
        boolean seen = false;
        BlockPos best = null;
        Comparator<BlockPos> comparator = Comparator.comparingDouble(pos::distSqr);
        for (BlockPos blockPos : destination)
        {
            if (!seen || comparator.compare(blockPos, best) < 0)
            {
                seen = true;
                best = blockPos;
            }
        }
        return Math.sqrt((seen ? best : pos).distSqr(pos));
    }
}
