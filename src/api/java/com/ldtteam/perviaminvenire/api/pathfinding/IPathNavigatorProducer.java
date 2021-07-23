package com.ldtteam.perviaminvenire.api.pathfinding;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;

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
    Optional<PathNavigation> get(final Mob entity, final PathNavigation initialNavigator);
}
