package com.ldtteam.perviaminvenire.api.adapters.start;

import com.ldtteam.perviaminvenire.api.pathfinding.AbstractPathJob;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Optional;

public class EntityInNoneFullBlockAdapter implements IStartPositionAdapter
{
    @Override
    public Optional<BlockPos> apply(final AbstractPathJob job, final Entity entity, final BlockPos startPos)
    {
        final BlockPos start = entity.blockPosition();
        final BlockState blockState = entity.getLevel().getBlockState(start);

        final VoxelShape collisionShape = blockState.getCollisionShape(entity.level, start);
        if (blockState.getMaterial().blocksMotion() && collisionShape.max(Direction.Axis.Y) > 0)
        {
            final double relPosX = Math.abs(entity.getX() % 1);
            final double relPosZ = Math.abs(entity.getZ() % 1);

            for (final AABB box : collisionShape.toAabbs())
            {
                if (relPosX >= box.minX && relPosX <= box.maxX
                      && relPosZ >= box.minZ && relPosZ <= box.maxZ
                      && box.maxY > 0)
                {
                    return Optional.of(new BlockPos(startPos.getX(), startPos.getY() + 1, startPos.getZ()));
                }
            }
        }

        return Optional.empty();
    }
}
