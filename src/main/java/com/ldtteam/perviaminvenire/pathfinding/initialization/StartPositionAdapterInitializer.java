package com.ldtteam.perviaminvenire.pathfinding.initialization;

import com.ldtteam.perviaminvenire.api.adapters.registry.IStartPositionAdapterRegistry;
import com.ldtteam.perviaminvenire.api.adapters.start.EntityInFenceAdapter;
import com.ldtteam.perviaminvenire.api.adapters.start.EntityInWaterAdapter;

public final class StartPositionAdapterInitializer {

    private StartPositionAdapterInitializer() {
        throw new IllegalStateException("Tried to initialize: StartPositionAdapterInitializer but this is a Utility class.");
    }

    @SuppressWarnings("unchecked")
    public static void setup()
    {
        IStartPositionAdapterRegistry.getInstance().register(
                        new EntityInWaterAdapter(),
                        new EntityInFenceAdapter()
        );
    }
}
