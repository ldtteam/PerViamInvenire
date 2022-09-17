package com.ldtteam.perviaminvenire.api.adapters.start;

import java.util.Optional;

import com.ldtteam.perviaminvenire.api.pathfinding.AbstractPathJob;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.BlockPos;

public class EntityInWaterAdapter implements IStartPositionAdapter {

    @Override
    public Optional<BlockPos> apply(final AbstractPathJob job, final Entity entity, final BlockPos startPos) {
        BlockPos workingPos = startPos;
        BlockState liquidState = entity.getCommandSenderWorld().getBlockState(workingPos);

        if (!liquidState.getMaterial().isLiquid())
            return Optional.empty();

        while (liquidState.getMaterial().isLiquid())
        {
            workingPos = workingPos.above();
            liquidState = entity.getCommandSenderWorld().getBlockState(workingPos);
        }

        return Optional.of(workingPos);
    }
}
