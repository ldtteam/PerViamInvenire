package com.ldtteam.perviaminvenire.pathfinding.registry;

import java.util.List;
import java.util.Optional;

import com.ldtteam.perviaminvenire.api.adapters.passable.IPassableBlockCallback;
import com.ldtteam.perviaminvenire.api.adapters.registry.IPassableBlockRegistry;
import com.ldtteam.perviaminvenire.api.adapters.registry.IWalkableBlockRegistry;
import com.ldtteam.perviaminvenire.api.adapters.walkable.IWalkableBlockCallback;
import com.ldtteam.perviaminvenire.api.pathfinding.PathingOptions;
import com.ldtteam.perviaminvenire.api.pathfinding.SurfaceType;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;

public final class WalkableBlockRegistry extends AbstractCallbackBasedRegistry<IWalkableBlockRegistry, IWalkableBlockCallback> implements IWalkableBlockRegistry {

    private static final WalkableBlockRegistry INSTANCE = new WalkableBlockRegistry();

    public static WalkableBlockRegistry getInstance() {
        return INSTANCE;
    }

    private WalkableBlockRegistry() {
    }

    @Override
    public IWalkableBlockRegistry getThis() {
        return this;
    }

    @Override
    protected IWalkableBlockCallback getRunnerInternal(final List<IWalkableBlockCallback> callbacks) {
        return (options, entity, state, pos) -> callbacks.stream().map(w -> w.get(options, entity, state, pos)).filter(Optional::isPresent).map(Optional::get).findFirst();
    }
}
