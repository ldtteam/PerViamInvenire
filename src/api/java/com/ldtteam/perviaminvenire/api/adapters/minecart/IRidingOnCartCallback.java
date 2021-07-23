package com.ldtteam.perviaminvenire.api.adapters.minecart;

import java.util.Optional;

import com.ldtteam.perviaminvenire.api.pathfinding.PathPointExtended;
import com.ldtteam.perviaminvenire.api.pathfinding.PathingOptions;

import net.minecraft.world.entity.Entity;

/**
 * This interface functions as a callback when the path navigate is handling,
 * pathing on rails. Allows for the spawning and despawning of carts when
 * needed.
 *
 * If {@link PathingOptions#canUseRails()} is true then there needs to be at least one
 * {@link IRidingOnCartCallback} registered.
 */
@FunctionalInterface
public interface IRidingOnCartCallback {

    Optional<Boolean> handle(final Entity entity, final PathPointExtended currentPathPoint, final PathPointExtended nextPathPoint);
}
