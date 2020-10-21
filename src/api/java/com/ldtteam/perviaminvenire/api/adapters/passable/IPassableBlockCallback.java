package com.ldtteam.perviaminvenire.api.adapters.passable;

import com.ldtteam.perviaminvenire.api.pathfinding.PathingOptions;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;

/**
 * This interface determines if a given block is passable
 * for a given entity.
 */
@FunctionalInterface
public interface IPassableBlockCallback {

    boolean isPassable(final PathingOptions pathingOptions, final Entity entity, final BlockState block, final boolean head);
}
