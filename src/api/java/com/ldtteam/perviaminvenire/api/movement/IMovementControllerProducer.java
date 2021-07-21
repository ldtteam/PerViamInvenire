package com.ldtteam.perviaminvenire.api.movement;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.controller.MovementController;
import net.minecraft.pathfinding.PathNavigator;

import java.util.Optional;

/**
 * Callback used to construct a movement controller instance of a given Mob.
 */
@FunctionalInterface
public interface IMovementControllerProducer
{
    /**
     * Invoked by the registry to get the updated movement controller for a given entity.
     *
     * @param entity The entity to get the controller for.
     * @param initialController The initial controller.
     * @return The updated controller
     */
    Optional<MovementController> get(final MobEntity entity, final MovementController initialController);
}
