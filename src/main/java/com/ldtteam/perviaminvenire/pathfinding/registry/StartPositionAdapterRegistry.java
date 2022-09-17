package com.ldtteam.perviaminvenire.pathfinding.registry;

import com.ldtteam.perviaminvenire.api.adapters.registry.IStartPositionAdapterRegistry;
import com.ldtteam.perviaminvenire.api.adapters.start.IStartPositionAdapter;
import net.minecraft.core.BlockPos;

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
        return (job, entity, start) -> {
            BlockPos current = start;
            boolean anyMatched = false;
            for (final IStartPositionAdapter adapter : callbacks) {
                 final Optional<BlockPos> result = adapter.apply(job, entity, current);
                 if (result.isPresent()) {
                     current = result.get();
                     anyMatched = true;
                 }
            }
            return anyMatched ? Optional.of(current) : Optional.empty();
        };
    }
}
