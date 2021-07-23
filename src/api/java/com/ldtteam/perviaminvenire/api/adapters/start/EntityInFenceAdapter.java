package com.ldtteam.perviaminvenire.api.adapters.start;

import static com.ldtteam.perviaminvenire.api.util.constants.PathingConstants.TOO_CLOSE_TO_FENCE;
import static com.ldtteam.perviaminvenire.api.util.constants.PathingConstants.TOO_FAR_FROM_FENCE;

import java.util.Optional;

import com.ldtteam.perviaminvenire.api.pathfinding.AbstractPathJob;

import net.minecraft.world.entity.Entity;
import net.minecraft.core.BlockPos;

public class EntityInFenceAdapter implements IStartPositionAdapter {
    @Override
    public Optional<BlockPos> apply(final AbstractPathJob job, final Entity entity) {
        BlockPos entityPos = entity.blockPosition();

        //Push away from fence
        final double dX = entity.getX() - Math.floor(entity.getX());
        final double dZ = entity.getZ() - Math.floor(entity.getZ());

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

        if (entityPos == entity.blockPosition())
            return Optional.empty();

        return Optional.of(entityPos);
    }
}
