package com.ldtteam.perviaminvenire.api.adapters.ladder;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

import java.util.Optional;

/**
 * Callback used to check if a given block is considered a ladder for a given entity.
 */
@FunctionalInterface
public interface IIsLadderBlockCallback
{
    /**
     * Indicates if this is a ladder or not.
     *
     * @param entity The entity.
     * @param block The block.
     * @param worldReader The world.
     * @param blockPos The pos.
     * @return An optional indicating if this is a ladder, not a ladder, or this callback does not know or care.
     */
    Optional<Boolean> isLadder(final Entity entity, final BlockState block, final IWorldReader worldReader, final BlockPos blockPos);
}
