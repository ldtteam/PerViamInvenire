package com.ldtteam.perviaminvenire.api.adapters.walkable;

import java.util.Optional;

import com.ldtteam.perviaminvenire.api.pathfinding.PathingOptions;
import com.ldtteam.perviaminvenire.api.pathfinding.SurfaceType;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

/**
 * This interface determines if a given block can be walked upon.
 */
public interface IWalkableBlockCallback {

    Optional<SurfaceType> get(final PathingOptions options, final Entity entity, final BlockState state, final BlockPos pos);
}
