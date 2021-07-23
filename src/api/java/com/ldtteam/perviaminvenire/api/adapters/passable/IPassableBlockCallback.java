package com.ldtteam.perviaminvenire.api.adapters.passable;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

/**
 * This interface determines if a given block is passable
 * for a given entity.
 */
@FunctionalInterface
public interface IPassableBlockCallback {

    Optional<Boolean> isPassable(final Entity entity, final BlockState block);
}
