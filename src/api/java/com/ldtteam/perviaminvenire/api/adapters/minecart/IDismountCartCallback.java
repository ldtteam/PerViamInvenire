package com.ldtteam.perviaminvenire.api.adapters.minecart;

import java.util.Optional;

import com.ldtteam.perviaminvenire.api.pathfinding.PathPointExtended;
import com.ldtteam.perviaminvenire.api.pathfinding.PathingOptions;

import net.minecraft.world.entity.Entity;

/**
 * Callback invoked to handle the dismounting of an entity riding a minecart.
 *
 * If {@link PathingOptions#canUseRails()} is true then there needs to be at least one
 * {@link IRidingOnCartCallback} registered.
 */
@FunctionalInterface
public interface IDismountCartCallback {
    Optional<Boolean> handle(final Entity entity, final Entity ridingEntity, final PathPointExtended currentPathPoint);
}
