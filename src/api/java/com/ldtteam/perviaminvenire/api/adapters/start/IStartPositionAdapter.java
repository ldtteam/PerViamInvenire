package com.ldtteam.perviaminvenire.api.adapters.start;

import com.ldtteam.perviaminvenire.api.pathfinding.AbstractPathJob;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

/**
 * Functional interface used for handling adaptations to the start position of
 * a path job.
 * This call is not threadsafe!
 */
@FunctionalInterface
public interface IStartPositionAdapter {

    IStartPositionAdapter IDENTITY = (job, entity) -> entity.getPosition();

    BlockPos apply(final AbstractPathJob job, final Entity entity);
}
