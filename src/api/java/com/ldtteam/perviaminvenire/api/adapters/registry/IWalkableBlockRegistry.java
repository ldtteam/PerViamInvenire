package com.ldtteam.perviaminvenire.api.adapters.registry;

import com.ldtteam.perviaminvenire.api.IPerViamInvenireApi;
import com.ldtteam.perviaminvenire.api.adapters.walkable.IWalkableBlockCallback;

import com.ldtteam.perviaminvenire.api.util.ICallbackBasedRegistry;

public interface IWalkableBlockRegistry extends ICallbackBasedRegistry<IWalkableBlockRegistry, IWalkableBlockCallback>
{

    static IWalkableBlockRegistry getInstance() {
        return IPerViamInvenireApi.getInstance().getWalkableBlockRegistry();
    }
}
