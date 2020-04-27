package com.ldtteam.perviaminvenire.pathfinding.registry;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.Sets;
import com.ldtteam.perviaminvenire.api.adapters.passable.IPassableBlockCallback;
import com.ldtteam.perviaminvenire.api.adapters.registry.IPassableBlockRegistry;
import com.ldtteam.perviaminvenire.api.pathfinding.PathingOptions;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;

public final class PassableBlockRegistry implements IPassableBlockRegistry {

    private static final PassableBlockRegistry INSTANCE = new PassableBlockRegistry();

    public static PassableBlockRegistry getInstance() {
        return INSTANCE;
    }

    private final Set<IPassableBlockCallback> passableBlocks = Sets.newConcurrentHashSet();

    private PassableBlockRegistry() {
    }

    @Override
    public IPassableBlockRegistry registerPassableBlocks(final Collection<IPassableBlockCallback> blocks) {
        this.passableBlocks.addAll(blocks);
        return this;
    }

    @Override
    public boolean isPassableBlock(final PathingOptions pathingOptions, final Entity entity, final BlockState block, final boolean head) {
        return passableBlocks.stream().anyMatch(c -> c.isPassable(pathingOptions, entity, block, head));
    }

}
