package com.ldtteam.com.perviaminvenire.pathfinder;

import net.minecraft.entity.MobEntity;
import net.minecraft.pathfinding.NodeProcessor;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

import javax.annotation.Nullable;
import java.util.Set;

public class CacheAccessingPathFinder extends PathFinder
{
    public CacheAccessingPathFinder(final NodeProcessor nodeProcessor, final int maxSearchRange)
    {
        super(nodeProcessor, maxSearchRange);
    }

    @Nullable
    @Override
    //Calculate path.
    public Path func_224775_a(
      final IWorldReader worldReader,
      final MobEntity entity,
      final Set<BlockPos> pathVia,
      final float p_224775_4_,
      final int p_224775_5_)
    {
        return super.func_224775_a(worldReader, entity, pathVia, p_224775_4_, p_224775_5_);
    }
}
