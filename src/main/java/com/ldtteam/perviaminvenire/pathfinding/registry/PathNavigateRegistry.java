package com.ldtteam.perviaminvenire.pathfinding.registry;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import com.google.common.collect.Maps;
import com.ldtteam.perviaminvenire.api.pathfinding.AbstractAdvancedGroundPathNavigate;
import com.ldtteam.perviaminvenire.api.pathfinding.registry.IPathNavigateRegistry;
import com.ldtteam.perviaminvenire.pathfinding.PerViamInvenireGroundPathNavigate;

import net.minecraft.entity.MobEntity;

public final class PathNavigateRegistry implements IPathNavigateRegistry {
    private static final Function<MobEntity, AbstractAdvancedGroundPathNavigate> DEFAULT = (entityLiving -> new PerViamInvenireGroundPathNavigate(entityLiving, entityLiving.world));

    private static final PathNavigateRegistry INSTANCE = new PathNavigateRegistry();

    public static PathNavigateRegistry getInstance() {
        return INSTANCE;
    }

    private final Map<Predicate<MobEntity>, Function<MobEntity, AbstractAdvancedGroundPathNavigate>> registry = Maps.newConcurrentMap();

    private PathNavigateRegistry() {
    }

    @Override
    public IPathNavigateRegistry registerNewPathNavigate(
                    final Predicate<MobEntity> selectionPredicate, final Function<MobEntity, AbstractAdvancedGroundPathNavigate> navigateProducer)
    {
        registry.put(selectionPredicate, navigateProducer);
        return this;
    }

    @Override
    public AbstractAdvancedGroundPathNavigate getNavigateFor(final MobEntity entityLiving)
    {
        return this.registry.keySet().stream().filter(predicate -> predicate.test(entityLiving)).findFirst().map(registry::get).orElse(DEFAULT).apply(entityLiving);
    }
}
