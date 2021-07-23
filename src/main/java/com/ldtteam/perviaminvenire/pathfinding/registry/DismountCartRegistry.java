package com.ldtteam.perviaminvenire.pathfinding.registry;

import java.util.List;
import java.util.Optional;

import com.ldtteam.perviaminvenire.api.adapters.minecart.IDismountCartCallback;
import com.ldtteam.perviaminvenire.api.adapters.registry.IDismountCartRegistry;

public final class DismountCartRegistry extends AbstractCallbackBasedRegistry<IDismountCartRegistry, IDismountCartCallback> implements IDismountCartRegistry
{

    private static final DismountCartRegistry INSTANCE = new DismountCartRegistry();

    public static DismountCartRegistry getInstance() {
        return INSTANCE;
    }

    private DismountCartRegistry() {
    }

    @Override
    public IDismountCartRegistry getThis() {
        return this;
    }

    @Override
    protected IDismountCartCallback getRunnerInternal(final List<IDismountCartCallback> callbacks) {
        return (entity, ridingEntity, currentPathPoint) -> callbacks.stream().map(callback -> callback.handle(entity, ridingEntity, currentPathPoint)).filter(Optional::isPresent).map(Optional::get).findFirst();
    }
}
