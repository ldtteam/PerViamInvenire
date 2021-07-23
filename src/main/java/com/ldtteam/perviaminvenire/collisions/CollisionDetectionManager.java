package com.ldtteam.perviaminvenire.collisions;

import com.ldtteam.perviaminvenire.api.adapters.registry.IBoundingBoxProducerRegistry;
import com.ldtteam.perviaminvenire.api.adapters.registry.IPassableBlockRegistry;
import com.ldtteam.perviaminvenire.api.collisions.ICollisionDetectionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
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
    public boolean canFit(final Entity entity, final Vector3d center, final Vector3d facing, final IWorldReader world)
    {
        final AxisAlignedBB entityBox = IBoundingBoxProducerRegistry.getInstance()
          .getRunner().produce(entity, center, facing, world)
          .orElseGet(() -> {
              final EntitySize entitySize = entity.getDimensions(entity.getPose());
              final float entityHorizontalSize = entitySize.width > 0.75F ? entitySize.width / 2.0F : 0.75F - entitySize.width / 2.0F;;

              return AxisAlignedBB.ofSize(entityHorizontalSize, 0.1F, entityHorizontalSize).move(center.x(), center.y() + (entity.getEyeHeight(entity.getPose()) - entitySize.height / 2), center.z());
          });

        if (hasNoCollisions(entity, world, entityBox))
        {
            return true;
        }

        final AxisAlignedBB bottomBox = new AxisAlignedBB(entityBox.minX, entityBox.minY, entityBox.minZ, entityBox.maxX, entityBox.minY + 1, entityBox.maxZ);
        final double maxHeightOfBottom = world.getBlockCollisions(null, bottomBox, (blockState, blockPos) -> !IPassableBlockRegistry.getInstance().getRunner().isPassable(entity, blockState).orElse(false))
                                             .mapToDouble(shape -> shape.max(Direction.Axis.Y) - bottomBox.minY)
                                             .max()
                                             .orElse(0d);
        if (maxHeightOfBottom >= 1 - bottomBox.minY)
        {
            return false;
        }

        if(maxHeightOfBottom != 0d)
        {
            final AxisAlignedBB entityStandingOnBlockBox = entityBox.move(0, maxHeightOfBottom, 0);
            if (hasNoCollisions(entity, world, entityStandingOnBlockBox))
            {
                return true;
            }
        }

        final AxisAlignedBB belowBox = bottomBox.move(0,-1,0);
        final double maxBlockHeightBelow = world.getBlockCollisions(null, belowBox, (blockState, blockPos) -> !IPassableBlockRegistry.getInstance().getRunner().isPassable(entity, blockState).orElse(false))
          .mapToDouble(shape -> shape.max(Direction.Axis.Y) - belowBox.minY)
          .max()
          .orElse(1d);

        final double toShift = 1d - maxBlockHeightBelow;
        if (toShift < 0.0001d)
            return false;

        final AxisAlignedBB shiftedBox = entityBox.move(0,-1 * toShift, 0);
        return hasNoCollisions(entity, world, shiftedBox);
    }

    public boolean hasNoCollisions(final Entity entity, final IWorldReader world, final AxisAlignedBB boundingBox)
    {
        return world.getBlockCollisions(
          entity,
          boundingBox,
          (blockState, blockPos) -> !IPassableBlockRegistry.getInstance().getRunner().isPassable(entity, blockState).orElse(false)
        ).allMatch(VoxelShape::isEmpty);
    }
}
