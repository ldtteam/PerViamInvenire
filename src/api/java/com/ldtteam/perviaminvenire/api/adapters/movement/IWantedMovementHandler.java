package com.ldtteam.perviaminvenire.api.adapters.movement;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;

/**
 * Custom wanted movement handler which handles indicating to an entities movement system what the next
 * target position is going to be.
 * <p>
 * Examples are squids which have custom movement handling via their {@link net.minecraft.world.entity.animal.Squid#setMovementVector(float, float, float)}
 */
@FunctionalInterface
public interface IWantedMovementHandler {

    /**
     * Configures the custom movement handler for the given entity.
     *
     * @param entity The entity to configure.
     * @param x The x coordinate of the wanted position.
     * @param y The y coordinate of the wanted position.
     * @param z The z coordinate of the wanted position.
     * @param speed The speed at which the entity should move.
     * @return {@code true} when custom logic was applied, {@code false} when no custom logic was applied.
     */
    boolean apply(final Entity entity, final double x, final double y, final double z, final double speed);
}
