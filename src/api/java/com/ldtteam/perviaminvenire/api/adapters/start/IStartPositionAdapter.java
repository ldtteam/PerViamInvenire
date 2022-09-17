package com.ldtteam.perviaminvenire.api.adapters.start;

import java.util.Optional;

import com.ldtteam.perviaminvenire.api.pathfinding.AbstractPathJob;

import net.minecraft.world.entity.Entity;
import net.minecraft.core.BlockPos;

/**
 * Functional interface used for handling adaptations to the start position of
 * a path job.
 * This call is not threadsafe!
 */
@FunctionalInterface
public interface IStartPositionAdapter {
    Optional<BlockPos> apply(final AbstractPathJob job, final Entity entity, final BlockPos current);
}
