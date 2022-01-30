package com.ldtteam.perviaminvenire.collisions;

import com.ldtteam.perviaminvenire.api.adapters.registry.IBoundingBoxProducerRegistry;
import com.ldtteam.perviaminvenire.api.adapters.registry.IPassableBlockRegistry;
import com.ldtteam.perviaminvenire.api.collisions.ICollisionDetectionManager;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

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
    public boolean canFit(final Entity entity, final Vec3 center, final Vec3 facing, final LevelReader world)
    {
        final AABB entityBox = IBoundingBoxProducerRegistry.getInstance()
          .getRunner().produce(entity, center, facing, world)
          .orElseGet(() -> {
              final EntityDimensions entitySize = entity.getDimensions(entity.getPose());
              final float entityHorizontalSize = entitySize.width > 0.75F ? entitySize.width / 2.0F : 0.75F - entitySize.width / 2.0F;

              return AABB.ofSize(Vec3.ZERO, entityHorizontalSize, 0.1F, entityHorizontalSize).move(center.x(), center.y() + (entity.getEyeHeight(entity.getPose()) - entitySize.height / 2), center.z());
          });

        if (hasNoCollisions(entity, world, entityBox))
        {
            return true;
        }

        final AABB bottomBox = new AABB(entityBox.minX, entityBox.minY, entityBox.minZ, entityBox.maxX, entityBox.minY + 1, entityBox.maxZ);
        final double maxHeightOfBottom = StreamSupport.stream(
                        Spliterators.spliteratorUnknownSize(new FilterableBlockCollisions(
                                world,
                                entity,
                                bottomBox,
                                (blockState, blockPos) -> !IPassableBlockRegistry.getInstance().getRunner().isPassable(entity, blockState).orElse(false)
                        ), Spliterator.ORDERED),
                        false)
                                             .mapToDouble(shape -> shape.max(Direction.Axis.Y) - bottomBox.minY)
                                             .max()
                                             .orElse(0d);
        if (maxHeightOfBottom >= 1 - bottomBox.minY)
        {
            return false;
        }

        if(maxHeightOfBottom != 0d)
        {
            final AABB entityStandingOnBlockBox = entityBox.move(0, maxHeightOfBottom, 0);
            if (hasNoCollisions(entity, world, entityStandingOnBlockBox))
            {
                return true;
            }
        }

        final AABB belowBox = bottomBox.move(0,-1,0);
        final double maxBlockHeightBelow = StreamSupport.stream(
                        Spliterators.spliteratorUnknownSize(new FilterableBlockCollisions(
                                world,
                                entity,
                                belowBox,
                                (blockState, blockPos) -> !IPassableBlockRegistry.getInstance().getRunner().isPassable(entity, blockState).orElse(false)
                        ), Spliterator.ORDERED),
                        false)
          .mapToDouble(shape -> shape.max(Direction.Axis.Y) - belowBox.minY)
          .max()
          .orElse(1d);

        final double toShift = 1d - maxBlockHeightBelow;
        if (toShift < 0.0001d)
            return false;

        final AABB shiftedBox = entityBox.move(0,-1 * toShift, 0);
        return hasNoCollisions(entity, world, shiftedBox);
    }

    public boolean hasNoCollisions(final Entity entity, final LevelReader world, final AABB boundingBox)
    {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(new FilterableBlockCollisions(
                        world,
                        entity,
                        boundingBox,
                        (blockState, blockPos) -> !IPassableBlockRegistry.getInstance().getRunner().isPassable(entity, blockState).orElse(false)
                ), Spliterator.ORDERED),
                false).allMatch(VoxelShape::isEmpty);
    }
}
