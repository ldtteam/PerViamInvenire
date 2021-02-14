package com.ldtteam.perviaminvenire.api.adapters.registry;

import com.ldtteam.perviaminvenire.api.IPerViamInvenireApi;
import com.ldtteam.perviaminvenire.api.adapters.passable.IPassableBlockCallback;

import com.ldtteam.perviaminvenire.api.util.ICallbackBasedRegistry;

public interface IPassableBlockRegistry extends ICallbackBasedRegistry<IPassableBlockRegistry, IPassableBlockCallback>
{
    static IPassableBlockRegistry getInstance() {
        return IPerViamInvenireApi.getInstance().getPassableBlockRegistry();
    }
}
