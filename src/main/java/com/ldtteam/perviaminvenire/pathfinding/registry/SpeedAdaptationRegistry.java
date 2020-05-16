package com.ldtteam.perviaminvenire.pathfinding.registry;

import java.util.List;
import java.util.Optional;

import com.ldtteam.perviaminvenire.api.adapters.registry.ISpeedAdaptationRegistry;
import com.ldtteam.perviaminvenire.api.adapters.speed.ISpeedAdaptationCallback;

import net.minecraft.entity.Entity;

public final class SpeedAdaptationRegistry extends AbstractCallbackBasedRegistry<ISpeedAdaptationRegistry, ISpeedAdaptationCallback> implements ISpeedAdaptationRegistry {

    private static final SpeedAdaptationRegistry INSTANCE = new SpeedAdaptationRegistry();

    public static SpeedAdaptationRegistry getInstance() {
        return INSTANCE;
    }

    private SpeedAdaptationRegistry() {
    }

    @Override
    public SpeedAdaptationRegistry getThis() {
        return this;
    }

    @Override
    protected ISpeedAdaptationCallback getRunnerInternal(final List<ISpeedAdaptationCallback> callbacks) {
        return (entity, walkSpeed) -> callbacks.stream().map(callback -> callback.get(entity, walkSpeed)).filter(Optional::isPresent).map(Optional::get).findFirst();
    }
}
