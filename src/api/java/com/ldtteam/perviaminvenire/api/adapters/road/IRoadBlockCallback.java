package com.ldtteam.perviaminvenire.api.adapters.road;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;

/**
 * This interface determines if a given block should be treated as a road
 * for a given entity.
 */
@FunctionalInterface
public interface IRoadBlockCallback {

    boolean isRoad(final Entity entity, final Block block);
}
