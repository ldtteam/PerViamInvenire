package com.ldtteam.perviaminvenire.api.collisions;

import com.ldtteam.perviaminvenire.api.IPerViamInvenireApi;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.Vec3;

public interface ICollisionDetectionManager
{

    static ICollisionDetectionManager getInstance()
    {
        return IPerViamInvenireApi.getInstance().getCollisionDetectionManager();
    }

    boolean canFit(Entity entity, Vec3 center, final Vec3 facing, LevelReader world);
}
