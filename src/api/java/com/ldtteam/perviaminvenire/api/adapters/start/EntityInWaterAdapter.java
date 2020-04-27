package com.ldtteam.perviaminvenire.api.adapters.start;

import com.ldtteam.perviaminvenire.api.pathfinding.AbstractPathJob;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

public class EntityInWaterAdapter implements IStartPositionAdapter {

    @Override
    public BlockPos apply(final AbstractPathJob job, final Entity entity) {
        BlockPos workingPos = entity.getPosition();
        BlockState liquidState = entity.getEntityWorld().getBlockState(workingPos);

        while (liquidState.getMaterial().isLiquid())
        {
            workingPos = workingPos.up();
            liquidState = entity.getEntityWorld().getBlockState(workingPos);
        }

        return workingPos;
    }
}
