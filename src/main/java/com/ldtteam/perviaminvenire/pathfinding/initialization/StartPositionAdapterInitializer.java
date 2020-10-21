package com.ldtteam.perviaminvenire.pathfinding.initialization;

import java.util.Optional;

import com.ldtteam.perviaminvenire.api.adapters.registry.IStartPositionAdapterRegistry;
import com.ldtteam.perviaminvenire.api.adapters.start.EntityInFenceAdapter;
import com.ldtteam.perviaminvenire.api.adapters.start.EntityInWaterAdapter;
import com.ldtteam.perviaminvenire.api.adapters.start.IStartPositionAdapter;
import com.ldtteam.perviaminvenire.api.pathfinding.AbstractPathJob;

import net.minecraft.block.FenceBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

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
