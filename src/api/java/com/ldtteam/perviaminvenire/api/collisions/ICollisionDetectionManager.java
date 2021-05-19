package com.ldtteam.perviaminvenire.api.collisions;

import com.ldtteam.perviaminvenire.api.IPerViamInvenireApi;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorldReader;

public interface ICollisionDetectionManager
{

    static ICollisionDetectionManager getInstance()
    {
        return IPerViamInvenireApi.getInstance().getCollisionDetectionManager();
    }

    boolean canFit(Entity entity, Vector3d center, final Vector3d facing, IWorldReader world);
}
