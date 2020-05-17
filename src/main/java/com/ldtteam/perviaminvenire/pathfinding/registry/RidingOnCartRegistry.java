package com.ldtteam.perviaminvenire.pathfinding.registry;

import java.util.List;
import java.util.Optional;

import com.ldtteam.perviaminvenire.api.adapters.minecart.IRidingOnCartCallback;
import com.ldtteam.perviaminvenire.api.adapters.registry.IRidingOnCartRegistry;
import com.ldtteam.perviaminvenire.api.pathfinding.PathPointExtended;

import net.minecraft.entity.Entity;

public class RidingOnCartRegistry extends AbstractCallbackBasedRegistry<IRidingOnCartRegistry, IRidingOnCartCallback> implements IRidingOnCartRegistry {

    private static final RidingOnCartRegistry INSTANCE = new RidingOnCartRegistry();

    public static RidingOnCartRegistry getInstance() {
        return INSTANCE;
    }

    private RidingOnCartRegistry() {
    }

    @Override
    public IRidingOnCartRegistry getThis() {
        return this;
    }

    @Override
    protected IRidingOnCartCallback getRunnerInternal(final List<IRidingOnCartCallback> callbacks) {
        return (entity, currentPathPoint, nextPathPoint) -> callbacks.stream().map(callback -> callback.handle(entity, currentPathPoint, nextPathPoint)).filter(Optional::isPresent).map(Optional::get).findFirst();
    }
}
