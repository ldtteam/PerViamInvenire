package com.ldtteam.perviaminvenire.pathfinding.registry;

import com.ldtteam.perviaminvenire.api.adapters.ladder.IIsLadderBlockCallback;
import com.ldtteam.perviaminvenire.api.adapters.registry.IIsLadderBlockRegistry;

import java.util.List;
import java.util.Optional;

public class IsLadderBlockRegistry extends AbstractCallbackBasedRegistry<IIsLadderBlockRegistry, IIsLadderBlockCallback> implements IIsLadderBlockRegistry {

    private static final IsLadderBlockRegistry INSTANCE = new IsLadderBlockRegistry();

    public static IsLadderBlockRegistry getInstance() {
        return INSTANCE;
    }

    private IsLadderBlockRegistry() {
    }

    @Override
    public IIsLadderBlockRegistry getThis() {
        return this;
    }

    @Override
    protected IIsLadderBlockCallback getRunnerInternal(final List<IIsLadderBlockCallback> callbacks) {
        return (entity, blockState, world, pos) -> callbacks.stream().map(c -> c.isLadder(entity, blockState, world, pos)).filter(Optional::isPresent).map(Optional::get).findFirst();
    }
}
