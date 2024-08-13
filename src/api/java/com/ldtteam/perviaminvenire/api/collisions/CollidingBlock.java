package com.ldtteam.perviaminvenire.api.collisions;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public record CollidingBlock(BlockState state, BlockPos pos) {
}
