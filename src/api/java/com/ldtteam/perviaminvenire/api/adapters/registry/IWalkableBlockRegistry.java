package com.ldtteam.perviaminvenire.api.adapters.registry;

import java.util.Arrays;
import java.util.Collection;

import com.ldtteam.perviaminvenire.api.IPerViamInvenireApi;
import com.ldtteam.perviaminvenire.api.adapters.walkable.IWalkableBlockCallback;
import com.ldtteam.perviaminvenire.api.pathfinding.PathingOptions;
import com.ldtteam.perviaminvenire.api.pathfinding.SurfaceType;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;

public interface IWalkableBlockRegistry extends ICallbackBasedRegistry<IWalkableBlockRegistry, IWalkableBlockCallback> {

    static IWalkableBlockRegistry getInstance() {
        return IPerViamInvenireApi.getInstance().getWalkableBlockRegistry();
    }
}
