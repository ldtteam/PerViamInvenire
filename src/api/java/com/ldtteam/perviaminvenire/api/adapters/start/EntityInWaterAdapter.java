package com.ldtteam.perviaminvenire.api.adapters.start;

import java.util.Optional;

import com.ldtteam.perviaminvenire.api.pathfinding.AbstractPathJob;

import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.BlockPos;

public class EntityInWaterAdapter implements IStartPositionAdapter {

    @Override
    public Optional<BlockPos> apply(final AbstractPathJob job, final Entity entity, final BlockPos startPos) {
        if (entity instanceof WaterAnimal)
            return Optional.empty();

        BlockPos workingPos = startPos;

        if (!job.isLiquid(workingPos))
            return Optional.empty();

        while (job.isLiquid(workingPos))
        {
            workingPos = workingPos.above();
        }

        return Optional.of(workingPos);
    }
}
