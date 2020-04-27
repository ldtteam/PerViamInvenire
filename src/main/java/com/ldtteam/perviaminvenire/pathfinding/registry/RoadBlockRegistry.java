package com.ldtteam.perviaminvenire.pathfinding.registry;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.Sets;
import com.ldtteam.perviaminvenire.api.adapters.registry.IRoadBlockRegistry;
import com.ldtteam.perviaminvenire.api.adapters.road.IRoadBlockCallback;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;

public final class RoadBlockRegistry implements IRoadBlockRegistry {

    private static final RoadBlockRegistry INSTANCE = new RoadBlockRegistry();

    public static RoadBlockRegistry getInstance() {
        return INSTANCE;
    }

    private final Set<IRoadBlockCallback> roadBlocks = Sets.newConcurrentHashSet();

    private RoadBlockRegistry() {
    }

    @Override
    public IRoadBlockRegistry registerRoadBlocks(final Collection<IRoadBlockCallback> blocks) {
        this.roadBlocks.addAll(blocks);
        return this;
    }

    @Override
    public boolean isRoadBlock(final Entity entity, final Block block) {
        return roadBlocks.stream().anyMatch(c -> c.isRoad(entity, block));
    }

}
