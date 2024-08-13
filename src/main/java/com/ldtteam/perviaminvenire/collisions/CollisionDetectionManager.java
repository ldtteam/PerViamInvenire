package com.ldtteam.perviaminvenire.collisions;

import com.ldtteam.perviaminvenire.api.adapters.registry.IBoundingBoxProducerRegistry;
import com.ldtteam.perviaminvenire.api.adapters.registry.IPassableBlockRegistry;
import com.ldtteam.perviaminvenire.api.collisions.CollidingBlock;
import com.ldtteam.perviaminvenire.api.collisions.ICollisionDetectionManager;
import com.ldtteam.perviaminvenire.util.CalculationNodeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
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
    public boolean canFit(final Entity entity, final BlockPos targetPos, final Vec3 facing, final LevelReader world, float fuzzRange)
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

        return hasNoCollisions(entity, world, centeredBox, fuzzRange);
    }

    @Override
    public Stream<CollidingBlock> getCollidingBlocks(Entity entity, BlockPos targetPos, Vec3 facing, LevelReader world) {
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

        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(new FilterableCollidingBlocks(
                        world,
                        entity,
                        centeredBox,
                        (blockState, blockPos) -> !IPassableBlockRegistry.getInstance().getRunner().isPassable(entity, blockState).orElse(false)
                ), Spliterator.ORDERED),
                false);
    }

    public boolean hasNoCollisions(final Entity entity, final LevelReader world, AABB boundingBox, float fuzzRange)
    {
        if (checkHasNoCollisions(entity, world, boundingBox)) return true;

        fuzzRange = Math.min(CalculationNodeUtils.DESTINATION_SLACK_ADJACENT, Math.abs(fuzzRange));
        fuzzRange = (float) Math.min(fuzzRange, Math.min(boundingBox.getXsize(), boundingBox.getZsize()) * 0.3f);

        final float fuzzStep = fuzzRange / 5;

        while(fuzzRange > 0) {
            boundingBox = boundingBox.inflate(fuzzStep / 2, 0, fuzzStep / 2);

            if (checkHasNoCollisions(entity, world, boundingBox)) return true;

            fuzzRange -= fuzzStep;
        }

        return false;
    }

    private static boolean checkHasNoCollisions(Entity entity, LevelReader world, AABB boundingBox) {
        if (StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(new FilterableBlockCollisions(
                        world,
                        entity,
                        boundingBox,
                        (blockState, blockPos) -> !IPassableBlockRegistry.getInstance().getRunner().isPassable(entity, blockState).orElse(false)
                ), Spliterator.ORDERED),
                false).allMatch(VoxelShape::isEmpty)) {
            return true;
        }
        return false;
    }
}
