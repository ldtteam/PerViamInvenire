package com.ldtteam.perviaminvenire.pathfinding.registry;

import com.ldtteam.perviaminvenire.api.adapters.registry.IStartPositionAdapterRegistry;
import com.ldtteam.perviaminvenire.api.adapters.start.IStartPositionAdapter;

import java.util.List;
import java.util.Optional;

public final class StartPositionAdapterRegistry extends AbstractCallbackBasedRegistry<IStartPositionAdapterRegistry, IStartPositionAdapter> implements IStartPositionAdapterRegistry {

    private static final StartPositionAdapterRegistry INSTANCE = new StartPositionAdapterRegistry();

    public static StartPositionAdapterRegistry getInstance() {
        return INSTANCE;
    }

    private StartPositionAdapterRegistry() {
    }

    @Override
    public IStartPositionAdapterRegistry getThis() {
        return this;
    }

    @Override
    protected IStartPositionAdapter getRunnerInternal(final List<IStartPositionAdapter> callbacks) {
        return (job, entity) -> callbacks.stream().map(a -> a.apply(job, entity)).filter(Optional::isPresent).map(Optional::get).findFirst();
    }
}
