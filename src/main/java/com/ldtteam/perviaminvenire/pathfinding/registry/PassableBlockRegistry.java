package com.ldtteam.perviaminvenire.pathfinding.registry;

import com.ldtteam.perviaminvenire.api.adapters.passable.IPassableBlockCallback;
import com.ldtteam.perviaminvenire.api.adapters.registry.IPassableBlockRegistry;

import java.util.List;
import java.util.Optional;

public final class PassableBlockRegistry extends AbstractCallbackBasedRegistry<IPassableBlockRegistry, IPassableBlockCallback> implements IPassableBlockRegistry {

    private static final PassableBlockRegistry INSTANCE = new PassableBlockRegistry();

    public static PassableBlockRegistry getInstance() {
        return INSTANCE;
    }

    private PassableBlockRegistry() {
    }

    @Override
    public IPassableBlockRegistry getThis() {
        return this;
    }

    @Override
    protected IPassableBlockCallback getRunnerInternal(final List<IPassableBlockCallback> callbacks) {
        return (entity, block) -> callbacks.stream().map(c -> c.isPassable(entity, block)).filter(Optional::isPresent).map(Optional::get).findFirst();
    }
}
