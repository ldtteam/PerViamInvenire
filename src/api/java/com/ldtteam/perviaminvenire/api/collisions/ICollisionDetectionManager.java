package com.ldtteam.perviaminvenire.api.collisions;

import com.ldtteam.perviaminvenire.api.IPerViamInvenireApi;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.Vec3;

import java.util.stream.Stream;

public interface ICollisionDetectionManager
{
    static ICollisionDetectionManager getInstance()
    {
        return IPerViamInvenireApi.getInstance().getCollisionDetectionManager();
    }

    boolean canFit(Entity entity, BlockPos targetPos, final Vec3 facing, LevelReader world, float fuzzRange);

    Stream<CollidingBlock> getCollidingBlocks(Entity entity, BlockPos targetPos, final Vec3 facing, LevelReader world);
}
