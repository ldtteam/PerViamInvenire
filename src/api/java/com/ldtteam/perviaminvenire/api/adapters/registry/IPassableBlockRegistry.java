package com.ldtteam.perviaminvenire.api.adapters.registry;

import java.util.Arrays;
import java.util.Collection;

import com.ldtteam.perviaminvenire.api.IPerViamInvenireApi;
import com.ldtteam.perviaminvenire.api.adapters.passable.IPassableBlockCallback;
import com.ldtteam.perviaminvenire.api.adapters.road.IRoadBlockCallback;
import com.ldtteam.perviaminvenire.api.pathfinding.PathingOptions;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;

public interface IPassableBlockRegistry {

    static IPassableBlockRegistry getInstance() {
        return IPerViamInvenireApi.getInstance().getPassableBlockRegistry();
    }

    default IPassableBlockRegistry registerPassableBlocks(final IPassableBlockCallback... blocks) {
        return this.registerPassableBlocks(Arrays.asList(blocks));
    }

    IPassableBlockRegistry registerPassableBlocks(final Collection<IPassableBlockCallback> blocks);

    boolean isPassableBlock(final PathingOptions pathingOptions, final Entity entity, final BlockState block, final boolean head);
}
