package com.ldtteam.perviaminvenire.pathfinding.registry;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import com.google.common.collect.Maps;
import com.ldtteam.perviaminvenire.api.adapters.minecart.IDismountCartCallback;
import com.ldtteam.perviaminvenire.api.adapters.registry.IDismountCartRegistry;
import com.ldtteam.perviaminvenire.api.pathfinding.AbstractAdvancedGroundPathNavigator;
import com.ldtteam.perviaminvenire.api.pathfinding.IPathNavigatorProducer;
import com.ldtteam.perviaminvenire.api.pathfinding.registry.IPathNavigatorRegistry;

import net.minecraft.entity.MobEntity;
import net.minecraft.pathfinding.PathNavigator;

public final class PathNavigatorRegistry extends AbstractCallbackBasedRegistry<IPathNavigatorRegistry, IPathNavigatorProducer> implements IPathNavigatorRegistry
{
    private static final PathNavigatorRegistry INSTANCE = new PathNavigatorRegistry();

    public static PathNavigatorRegistry getInstance() {
        return INSTANCE;
    }

    private final Map<Predicate<MobEntity>, Function<MobEntity, PathNavigator>> registry = Maps.newConcurrentMap();

    private PathNavigatorRegistry() {
    }

    @Override
    public IPathNavigatorRegistry getThis() {
        return this;
    }

    @Override
    protected IPathNavigatorProducer getRunnerInternal(final List<IPathNavigatorProducer> callbacks) {
        return (entity, initialNavigator) -> callbacks.stream().map(callback -> callback.get(entity, initialNavigator)).filter(Optional::isPresent).map(Optional::get).findFirst();
    }
}
