package com.ldtteam.perviaminvenire.pathfinding;

import com.ldtteam.perviaminvenire.api.pathfinding.AbstractPathJob;
import com.ldtteam.perviaminvenire.api.pathfinding.PathResult;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * PVI async climber PathNavigator.
 */
public class PerViamInvenireClimberPathNavigator extends PerViamInvenireGroundPathNavigator
{

    private BlockPos targetPosition = null;

    public PerViamInvenireClimberPathNavigator(@NotNull final MobEntity entity)
    {
        super(entity);
    }

    public PerViamInvenireClimberPathNavigator(@NotNull final MobEntity entity, final World world)
    {
        super(entity, world);
    }

    @Nullable
    @Override
    public PathResult<? extends AbstractPathJob> setPathJob(
      @NotNull final AbstractPathJob job, final BlockPos dest, final double speed)
    {
        this.targetPosition = dest;
        return super.setPathJob(job, dest, speed);
    }

    public Path getPathToPos(@NotNull BlockPos pos, int accuracy) {
        this.targetPosition = pos;
        return super.getPathToPos(pos, accuracy);
    }

    /**
     * Returns a path to the given entity or null
     */
    public Path pathfind(Entity entityIn, int p_75494_2_) {
        this.targetPosition = entityIn.getPosition();
        return super.pathfind(entityIn, p_75494_2_);
    }

    /**
     * Try to find and set a path to EntityLiving. Returns true if successful. Args : entity, speed
     */
    public boolean tryMoveToEntityLiving(Entity entityIn, double speedIn) {
        Path path = this.pathfind(entityIn, 0);
        if (path != null) {
            return this.setPath(path, speedIn);
        } else {
            this.targetPosition = entityIn.getPosition();
            this.speed = speedIn;
            return true;
        }
    }

    public void tick() {
        if (!this.noPath()) {
            //Normal pathfinding, so we just follow that.
            super.tick();
        } else {
            //No path available. But we have a last position stored.
   /*         if (this.targetPosition != null) {
                // FORGE: Fix MC-94054
                if (!this.targetPosition.withinDistance(this.entity.getPositionVec(), Math.max((double)this.entity.getWidth(), 1.0D)) && (!(this.entity.getPosY() > (double)this.targetPosition.getY()) || !(new BlockPos((double)this.targetPosition.getX(), this.entity.getPosY(), (double)this.targetPosition.getZ())).withinDistance(this.entity.getPositionVec(), Math.max((double)this.entity.getWidth(), 1.0D)))) {
                    this.entity.getMoveHelper().setMoveTo((double)this.targetPosition.getX(), (double)this.targetPosition.getY(), (double)this.targetPosition.getZ(), this.speed);
                } else {
                    this.targetPosition = null;
                }
            }*/

        }
    }
}
