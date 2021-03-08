package com.ldtteam.perviaminvenire.api.adapters.passable;

import com.ldtteam.perviaminvenire.api.pathfinding.PathingOptions;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;

import java.util.Optional;

/**
 * This interface determines if a given block is passable
 * for a given entity.
 */
@FunctionalInterface
public interface IPassableBlockCallback {

    Optional<Boolean> isPassable(final Entity entity, final BlockState block);
}
