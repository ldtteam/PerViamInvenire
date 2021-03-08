package com.ldtteam.perviaminvenire.collisions;

import com.ldtteam.perviaminvenire.api.adapters.registry.IPassableBlockRegistry;
import com.ldtteam.perviaminvenire.api.collisions.ICollisionDetectionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

public class CollisionDetectionManager implements ICollisionDetectionManager
{
    private static final CollisionDetectionManager INSTANCE = new CollisionDetectionManager();

    public static CollisionDetectionManager getInstance()
    {
        return INSTANCE;
    }

    private CollisionDetectionManager()
    {
    }

    @Override
    public boolean canFit(final Entity entity, final BlockPos center, final IWorldReader world)
    {
        final EntitySize entitySize = entity.getSize(entity.getPose());
        final AxisAlignedBB entityBox = entitySize.func_242285_a(center.getX() + 0.5d, center.getY(), center.getZ() + 0.5d);

        if (hasNoCollisions(entity, world, entityBox))
        {
            return true;
        }

        final AxisAlignedBB bottomBox = new AxisAlignedBB(entityBox.minX, entityBox.minY, entityBox.minZ, entityBox.maxX, entityBox.minY + 1, entityBox.maxZ);
        final double maxHeightOfBottom = world.func_241457_a_(null, bottomBox, (blockState, blockPos) -> !IPassableBlockRegistry.getInstance().getRunner().isPassable(entity, blockState).orElse(false))
                                             .mapToDouble(shape -> shape.getEnd(Direction.Axis.Y) - bottomBox.minY)
                                             .max()
                                             .orElse(0d);
        if (maxHeightOfBottom >= 0.99999d)
        {
            return false;
        }

        if(maxHeightOfBottom != 0d)
        {
            final AxisAlignedBB entityStandingOnBlockBox = entityBox.offset(0, maxHeightOfBottom, 0);
            if (hasNoCollisions(entity, world, entityStandingOnBlockBox))
            {
                return true;
            }
        }

        final AxisAlignedBB belowBox = bottomBox.offset(0,-1,0);
        final double maxBlockHeightBelow = world.func_241457_a_(null, belowBox, (blockState, blockPos) -> !IPassableBlockRegistry.getInstance().getRunner().isPassable(entity, blockState).orElse(false))
          .mapToDouble(shape -> shape.getEnd(Direction.Axis.Y) - belowBox.minY)
          .max()
          .orElse(1d);

        final double toShift = 1d - maxBlockHeightBelow;
        if (toShift < 0.0001d)
            return false;

        final AxisAlignedBB shiftedBox = entityBox.offset(0,-1 * toShift, 0);
        return hasNoCollisions(entity, world, shiftedBox);
    }

    public boolean hasNoCollisions(final Entity entity, final IWorldReader world, final AxisAlignedBB boundingBox)
    {
        return world.func_241457_a_(
          entity,
          boundingBox,
          (blockState, blockPos) -> !IPassableBlockRegistry.getInstance().getRunner().isPassable(entity, blockState).orElse(false)
        ).allMatch(voxelShape -> voxelShape.isEmpty());
    }
}
