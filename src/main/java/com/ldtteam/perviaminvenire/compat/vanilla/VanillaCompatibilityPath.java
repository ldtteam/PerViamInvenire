package com.ldtteam.perviaminvenire.compat.vanilla;

import com.ldtteam.perviaminvenire.api.pathfinding.PathPointExtended;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.concurrent.Future;

public class VanillaCompatibilityPath extends Path
{

    private static final Logger LOGGER = LogManager.getLogger();

    @NotNull
    private final Future<Path> calculationFuture;

    private boolean isCalculationComplete;

    private static PathPointExtended calculatePathTo(final BlockPos source, final BlockPos target) {
        final BlockPos delta = target.subtract(source);
        return new PathPointExtended(
          target.add(Direction.getFacingFromVector(delta.getX(), delta.getY(), delta.getZ()).getDirectionVec())
        );
    }

    public VanillaCompatibilityPath(
      final BlockPos source,
      final BlockPos target,
      @NotNull final Future<Path> calculationFuture)
    {
        super(Collections.singletonList(calculatePathTo(source, target)),
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
        this.field_224774_h = calculatedPath.field_224774_h;

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

        this.field_224774_h = false;
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
}
