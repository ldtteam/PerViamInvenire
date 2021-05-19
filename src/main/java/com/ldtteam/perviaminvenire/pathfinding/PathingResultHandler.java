package com.ldtteam.perviaminvenire.pathfinding;

import com.ldtteam.perviaminvenire.api.pathfinding.ICalculationResultTracker;
import com.ldtteam.perviaminvenire.api.pathfinding.IPathingResultHandler;
import com.ldtteam.perviaminvenire.api.pathfinding.PathingCalculationData;
import net.minecraft.entity.Entity;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

public class PathingResultHandler implements IPathingResultHandler
{
    private static final PathingResultHandler INSTANCE = new PathingResultHandler();

    public static PathingResultHandler getInstance()
    {
        return INSTANCE;
    }


    private PathingResultHandler()
    {
    }

    @Override
    public void onCompleted(final PathingCalculationData data, final Entity entity, final ServerWorld world)
    {
        ICalculationResultTracker.getInstance().getTrackingPlayers(entity)
          .forEach(player -> CalculationResultRenderer.getInstance().renderDataIntoWorld(data, world));
    }


}
