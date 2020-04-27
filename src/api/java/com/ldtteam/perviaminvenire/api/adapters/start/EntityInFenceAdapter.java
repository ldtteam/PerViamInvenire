package com.ldtteam.perviaminvenire.api.adapters.start;

import static com.ldtteam.perviaminvenire.api.util.constants.PathingConstants.TOO_CLOSE_TO_FENCE;
import static com.ldtteam.perviaminvenire.api.util.constants.PathingConstants.TOO_FAR_FROM_FENCE;

import com.ldtteam.perviaminvenire.api.pathfinding.AbstractPathJob;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

public class EntityInFenceAdapter implements IStartPositionAdapter {
    @Override
    public BlockPos apply(final AbstractPathJob job, final Entity entity) {
        BlockPos entityPos = entity.getPosition();

        //Push away from fence
        final double dX = entity.getPosX() - Math.floor(entity.getPosX());
        final double dZ = entity.getPosZ() - Math.floor(entity.getPosZ());

        if (dX < TOO_CLOSE_TO_FENCE)
        {
            entityPos = entityPos.west();
        }
        else if (dX > TOO_FAR_FROM_FENCE)
        {
            entityPos = entityPos.east();
        }

        if (dZ < TOO_CLOSE_TO_FENCE)
        {
            entityPos = entityPos.north();
        }
        else if (dZ > TOO_FAR_FROM_FENCE)
        {
            entityPos = entityPos.south();
        }
        
        return entityPos;
    }
}
