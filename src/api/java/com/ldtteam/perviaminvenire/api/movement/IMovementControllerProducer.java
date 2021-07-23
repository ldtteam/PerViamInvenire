package com.ldtteam.perviaminvenire.api.movement;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.MoveControl;
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
    Optional<MoveControl> get(final Mob entity, final MoveControl initialController);
}
