package com.ldtteam.perviaminvenire.pathfinding.registry;

import com.google.common.collect.Maps;
import com.ldtteam.perviaminvenire.api.movement.IMovementControllerProducer;
import com.ldtteam.perviaminvenire.api.movement.registry.IMovementControllerRegistry;
import net.minecraft.world.entity.Mob;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public final class MovementControllerRegistry extends AbstractCallbackBasedRegistry<IMovementControllerRegistry, IMovementControllerProducer> implements IMovementControllerRegistry
{
    private static final MovementControllerRegistry INSTANCE = new MovementControllerRegistry();

    public static MovementControllerRegistry getInstance() {
        return INSTANCE;
    }

    private MovementControllerRegistry() {
    }

    @Override
    public IMovementControllerRegistry getThis() {
        return this;
    }

    @Override
    protected IMovementControllerProducer getRunnerInternal(final List<IMovementControllerProducer> callbacks) {
        return (entity, initialNavigator) -> callbacks.stream().map(callback -> callback.get(entity, initialNavigator)).filter(Optional::isPresent).map(Optional::get).findFirst();
    }
}
