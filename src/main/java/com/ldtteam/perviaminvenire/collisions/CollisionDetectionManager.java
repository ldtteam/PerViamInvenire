package com.ldtteam.perviaminvenire.collisions;

import com.ldtteam.perviaminvenire.api.adapters.registry.IBoundingBoxProducerRegistry;
import com.ldtteam.perviaminvenire.api.adapters.registry.IPassableBlockRegistry;
import com.ldtteam.perviaminvenire.api.collisions.ICollisionDetectionManager;
import net.minecraft.core.BlockPos;
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
    public boolean canFit(final Entity entity, final BlockPos targetPos, final Vec3 facing, final LevelReader world)
    {
        final AABB centeredEntityBox = IBoundingBoxProducerRegistry.getInstance()
          .getRunner().produce(entity)
          .orElseGet(() -> entity.getDimensions(entity.getPose()).makeBoundingBox(Vec3.ZERO));

        final AABB centeredBox = new AABB(
                targetPos.getX() + 0.5D - (centeredEntityBox.getXsize() / 2),
                targetPos.getY(),
                targetPos.getZ() + 0.5D - (centeredEntityBox.getZsize() / 2),
                targetPos.getX() + 0.5D + (centeredEntityBox.getXsize() / 2),
                targetPos.getY() + centeredEntityBox.getYsize(),
                targetPos.getZ() + 0.5D + (centeredEntityBox.getZsize() / 2));

        return hasNoCollisions(entity, world, centeredBox);
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
