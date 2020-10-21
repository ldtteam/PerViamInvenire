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

public interface IPassableBlockRegistry extends ICallbackBasedRegistry<IPassableBlockRegistry, IPassableBlockCallback> {
    static IPassableBlockRegistry getInstance() {
        return IPerViamInvenireApi.getInstance().getPassableBlockRegistry();
    }
}
