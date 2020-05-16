package com.ldtteam.perviaminvenire.pathfinding.registry;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import com.google.common.collect.Maps;
import com.ldtteam.perviaminvenire.api.adapters.start.IStartPositionAdapter;
import com.ldtteam.perviaminvenire.api.adapters.registry.IStartPositionAdapterRegistry;
import com.ldtteam.perviaminvenire.api.pathfinding.AbstractPathJob;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

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
