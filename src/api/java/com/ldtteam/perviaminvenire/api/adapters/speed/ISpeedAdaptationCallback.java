package com.ldtteam.perviaminvenire.api.adapters.speed;

import java.util.Optional;

import net.minecraft.entity.Entity;

/**
 * A callback that adapts the walk speed of an entity to a different adapter.
 */
@FunctionalInterface
public interface ISpeedAdaptationCallback {

    Optional<Double> get(final Entity entity, final double walkSpeed);
}
