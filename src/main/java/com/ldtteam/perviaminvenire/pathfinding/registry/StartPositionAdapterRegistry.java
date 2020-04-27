package com.ldtteam.perviaminvenire.pathfinding.registry;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import com.google.common.collect.Maps;
import com.ldtteam.perviaminvenire.api.adapters.start.IStartPositionAdapter;
import com.ldtteam.perviaminvenire.api.adapters.registry.IStartPositionAdapterRegistry;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;

public final class StartPositionAdapterRegistry implements IStartPositionAdapterRegistry {

    private static final StartPositionAdapterRegistry INSTANCE = new StartPositionAdapterRegistry();

    public static StartPositionAdapterRegistry getInstance() {
        return INSTANCE;
    }

    private final Map<Predicate<Entity>, IStartPositionAdapter> entityAdapterMap = Maps.newConcurrentMap();
    private final Map<Predicate<Block>, IStartPositionAdapter> blockAdapterMap = Maps.newConcurrentMap();

    private StartPositionAdapterRegistry() {
    }

    @Override
    public IStartPositionAdapterRegistry registerForBlocks(
                    final IStartPositionAdapter adapter, final Collection<Predicate<Block>> predicates) {
        predicates.forEach(predicate -> {
            blockAdapterMap.putIfAbsent(predicate, adapter);
        });

        return this;
    }

    @Override
    public Optional<IStartPositionAdapter> getForBlock(final Block block) {
        return this.blockAdapterMap.keySet().stream()
                        .filter(blockPredicate -> blockPredicate.test(block))
                        .findFirst()
                        .map(this.blockAdapterMap::get);
    }

    @Override
    public IStartPositionAdapterRegistry registerForEntity(
                    final IStartPositionAdapter adapter, final Collection<Predicate<Entity>> predicates) {
        predicates.forEach(predicate -> {
            entityAdapterMap.putIfAbsent(predicate, adapter);
        });

        return this;
    }

    @Override
    public Optional<IStartPositionAdapter> getForEntity(final Entity entity) {
        return this.entityAdapterMap.keySet().stream()
                               .filter(entityPredicate -> entityPredicate.test(entity))
                               .findFirst()
                               .map(this.entityAdapterMap::get);
    }
}
