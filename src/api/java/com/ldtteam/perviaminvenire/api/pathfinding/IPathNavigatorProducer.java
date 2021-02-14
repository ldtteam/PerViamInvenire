package com.ldtteam.perviaminvenire.api.pathfinding;

import net.minecraft.entity.MobEntity;
import net.minecraft.pathfinding.PathNavigator;

import java.util.Optional;

/**
 * Callback used to construct a PathNavigate instance of a given Mob.
 */
@FunctionalInterface
public interface IPathNavigatorProducer
{
    /**
     * Invoked by the registry to get the updated path navigator for a given entity.
     *
     * @param entity The entity to get the navigator for.
     * @param initialNavigator The initial navigator.
     * @return The updated navigator
     */
    Optional<PathNavigator> get(final MobEntity entity, final PathNavigator initialNavigator);
}
