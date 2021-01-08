package com.ldtteam.perviaminvenire.pathfinding.registry;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import com.google.common.collect.Maps;
import com.ldtteam.perviaminvenire.api.pathfinding.AbstractAdvancedGroundPathNavigate;
import com.ldtteam.perviaminvenire.api.pathfinding.registry.IPathNavigateRegistry;
import com.ldtteam.perviaminvenire.pathfinding.PerViamInvenireGroundPathNavigate;

import net.minecraft.entity.MobEntity;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.pathfinding.PathNavigator;

public final class PathNavigateRegistry implements IPathNavigateRegistry {
    private static final Function<MobEntity, PathNavigator> DEFAULT = MobEntity::getNavigator;

    private static final PathNavigateRegistry INSTANCE = new PathNavigateRegistry();

    public static PathNavigateRegistry getInstance() {
        return INSTANCE;
    }

    private final Map<Predicate<MobEntity>, Function<MobEntity, PathNavigator>> registry = Maps.newConcurrentMap();

    private PathNavigateRegistry() {
    }

    @Override
    public IPathNavigateRegistry registerNewPathNavigate(
                    final Predicate<MobEntity> selectionPredicate, final Function<MobEntity, AbstractAdvancedGroundPathNavigate> navigateProducer)
    {
        registry.put(selectionPredicate, navigateProducer::apply);
        return this;
    }

    @Override
    public PathNavigator getNavigateFor(final MobEntity entityLiving)
    {
        return this.registry.keySet().stream().filter(predicate -> predicate.test(entityLiving)).findFirst().map(registry::get).orElse(DEFAULT).apply(entityLiving);
    }
}
