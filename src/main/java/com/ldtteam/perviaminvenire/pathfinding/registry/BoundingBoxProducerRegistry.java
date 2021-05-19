package com.ldtteam.perviaminvenire.pathfinding.registry;

import com.ldtteam.perviaminvenire.api.adapters.boundingbox.IBoundingBoxProducer;
import com.ldtteam.perviaminvenire.api.adapters.registry.IBoundingBoxProducerRegistry;

import java.util.List;
import java.util.Optional;

public class BoundingBoxProducerRegistry extends AbstractCallbackBasedRegistry<IBoundingBoxProducerRegistry, IBoundingBoxProducer> implements IBoundingBoxProducerRegistry
{

    private static final BoundingBoxProducerRegistry INSTANCE = new BoundingBoxProducerRegistry();

    public static BoundingBoxProducerRegistry getInstance() {
        return INSTANCE;
    }

    private BoundingBoxProducerRegistry() {
    }

    @Override
    public IBoundingBoxProducerRegistry getThis() {
        return this;
    }

    @Override
    protected IBoundingBoxProducer getRunnerInternal(final List<IBoundingBoxProducer> callbacks) {
        return (entity, center, facing, world) -> callbacks.stream().map(callback -> callback.produce(entity, center, facing, world)).filter(Optional::isPresent).map(Optional::get).findFirst();
    }
}