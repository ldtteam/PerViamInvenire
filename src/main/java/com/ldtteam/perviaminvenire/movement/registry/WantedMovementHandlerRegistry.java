package com.ldtteam.perviaminvenire.movement.registry;

import com.ldtteam.perviaminvenire.api.adapters.movement.IWantedMovementHandler;
import com.ldtteam.perviaminvenire.api.movement.registry.IWantedMovementHandlerRegistry;
import com.ldtteam.perviaminvenire.pathfinding.registry.AbstractCallbackBasedRegistry;
import net.minecraft.world.entity.Entity;

import java.util.List;

public final class WantedMovementHandlerRegistry extends AbstractCallbackBasedRegistry<IWantedMovementHandlerRegistry, IWantedMovementHandler> implements IWantedMovementHandlerRegistry {
    private static final WantedMovementHandlerRegistry INSTANCE = new WantedMovementHandlerRegistry();

    public static WantedMovementHandlerRegistry getInstance() {
        return INSTANCE;
    }

    private WantedMovementHandlerRegistry() {
    }

    @Override
    public IWantedMovementHandlerRegistry getThis() {
        return this;
    }

    @Override
    protected IWantedMovementHandler getRunnerInternal(List<IWantedMovementHandler> callbacks) {
        return (entity, x, y, z, speed) -> {
            for (IWantedMovementHandler callback : callbacks) {
                if (callback.apply(entity, x, y, z, speed)) {
                    return true;
                }
            }
            return false;
        };
    }
}
