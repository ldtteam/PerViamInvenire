package com.ldtteam.perviaminvenire.api.adapters.registry;

import java.util.Arrays;
import java.util.Collection;

import com.ldtteam.perviaminvenire.api.IPerViamInvenireApi;
import com.ldtteam.perviaminvenire.api.adapters.road.IRoadBlockCallback;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;

public interface IRoadBlockRegistry {

    static IRoadBlockRegistry getInstance() {
        return IPerViamInvenireApi.getInstance().getRoadBlockRegistry();
    }

    default IRoadBlockRegistry registerRoadBlocks(final IRoadBlockCallback... blocks) {
        return this.registerRoadBlocks(Arrays.asList(blocks));
    }

    IRoadBlockRegistry registerRoadBlocks(final Collection<IRoadBlockCallback> blocks);

    boolean isRoadBlock(final Entity entity, final Block block);
}
