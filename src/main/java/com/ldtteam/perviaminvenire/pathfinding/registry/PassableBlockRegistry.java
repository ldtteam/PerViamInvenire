package com.ldtteam.perviaminvenire.pathfinding.registry;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import com.ldtteam.perviaminvenire.api.adapters.passable.IPassableBlockCallback;
import com.ldtteam.perviaminvenire.api.adapters.registry.IPassableBlockRegistry;
import com.ldtteam.perviaminvenire.api.pathfinding.PathingOptions;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;

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
        return (pathingOptions, entity, block, head) -> callbacks.stream().anyMatch(c -> c.isPassable(pathingOptions, entity, block, head));
    }
}
