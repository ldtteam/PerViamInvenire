package com.ldtteam.perviaminvenire.api.collisions;

import com.ldtteam.perviaminvenire.api.IPerViamInvenireApi;
import net.minecraft.world.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.LevelReader;

public interface ICollisionDetectionManager
{

    static ICollisionDetectionManager getInstance()
    {
        return IPerViamInvenireApi.getInstance().getCollisionDetectionManager();
    }

    boolean canFit(Entity entity, Vec3 center, final Vec3 facing, LevelReader world);
}
