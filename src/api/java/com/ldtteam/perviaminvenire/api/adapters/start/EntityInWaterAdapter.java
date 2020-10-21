package com.ldtteam.perviaminvenire.api.adapters.start;

import java.util.Optional;

import com.ldtteam.perviaminvenire.api.pathfinding.AbstractPathJob;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

public class EntityInWaterAdapter implements IStartPositionAdapter {

    @Override
    public Optional<BlockPos> apply(final AbstractPathJob job, final Entity entity) {
        BlockPos workingPos = entity.getPosition();
        BlockState liquidState = entity.getEntityWorld().getBlockState(workingPos);

        if (!liquidState.getMaterial().isLiquid())
            return Optional.empty();

        while (liquidState.getMaterial().isLiquid())
        {
            workingPos = workingPos.up();
            liquidState = entity.getEntityWorld().getBlockState(workingPos);
        }

        return Optional.of(workingPos);
    }
}
