package com.ldtteam.perviaminvenire.compat.vanilla;

import com.google.common.collect.Lists;
import com.ldtteam.perviaminvenire.api.pathfinding.PathPointExtended;
import net.minecraft.entity.Entity;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

public class VanillaCompatibilityPath extends Path
{

    private static final Logger LOGGER = LogManager.getLogger();

    @NotNull
    private final Future<Path> calculationFuture;

    private boolean isCalculationComplete;

    private static List<PathPoint> calculatePathTo(final BlockPos source, final BlockPos target) {
        final BlockPos delta = target.subtract(source);

        return Lists.newArrayList(
          new PathPointExtended(
            target.add(Direction.getFacingFromVector(delta.getX(), delta.getY(), delta.getZ()).getDirectionVec())
          ),
          new PathPointExtended(
            target.add(Direction.getFacingFromVector(delta.getX(), delta.getY(), delta.getZ()).getDirectionVec())
              .add(Direction.getFacingFromVector(delta.getX(), delta.getY(), delta.getZ()).getDirectionVec())
          )
        );
    }

    public VanillaCompatibilityPath(
      final BlockPos source,
      final BlockPos target,
      @NotNull final Future<Path> calculationFuture)
    {
        super(calculatePathTo(source, target),
          target,
          true);

        this.calculationFuture = calculationFuture;
    }

    private boolean isNotComplete() {
        if (calculationFuture.isCancelled())
            return false;

        if (!calculationFuture.isDone())
            return true;

        if (isCalculationComplete)
            return false;

        final Path calculatedPath;
        try
        {
            calculatedPath = calculationFuture.get();
        }
        catch (Exception e)
        {
            LOGGER.error("Failed to get the calculated path even though it was specified to be complete.", e);
            return true;
        }

        isCalculationComplete = true;

        this.points = calculatedPath.points;
        this.openSet = calculatedPath.openSet;
        this.closedSet = calculatedPath.closedSet;
        this.currentPathIndex = calculatedPath.currentPathIndex;
        this.field_224773_g = calculatedPath.field_224773_g;
        this.reachesTargetFlag = calculatedPath.reachesTargetFlag;

        return false;
    }

    @Override
    public void incrementPathIndex()
    {
        if (isNotComplete())
            return;

        super.incrementPathIndex();
    }



    @Override
    public boolean func_242945_b()
    {
        if (isNotComplete())
            return true;

        return super.func_242945_b();
    }

    @Override
    public boolean isFinished()
    {
        if (isNotComplete())
            return false;

        return super.isFinished();
    }

    @Override
    public void setCurrentPathIndex(final int currentPathIndexIn)
    {
        if (isNotComplete())
            return;

        super.setCurrentPathIndex(currentPathIndexIn);
    }

    public void setCancelled() {
        if (isCalculationComplete)
            return;

        this.reachesTargetFlag = false;
        this.points = Collections.emptyList();
        this.currentPathIndex = 1;

        if (!this.calculationFuture.isDone())
            this.calculationFuture.cancel(true);
    }

    public BlockPos getDestination() {
        if (this.getFinalPathPoint() == null)
            return BlockPos.ZERO;

        return this.getFinalPathPoint().func_224759_a();
    }

    /**
     * Gets the vector of the PathPoint associated with the given index.
     *
     * @param entityIn The entity in question.
     * @param index The index in question.
     */
    @Override
    public Vector3d getVectorFromIndex(final Entity entityIn, final int index)
    {
        return super.getVectorFromIndex(entityIn, index);
    }
}
