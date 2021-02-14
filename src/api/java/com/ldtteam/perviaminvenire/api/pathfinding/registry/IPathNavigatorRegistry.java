package com.ldtteam.perviaminvenire.api.pathfinding.registry;

import com.ldtteam.perviaminvenire.api.IPerViamInvenireApi;
import com.ldtteam.perviaminvenire.api.pathfinding.IPathNavigatorProducer;
import com.ldtteam.perviaminvenire.api.util.ICallbackBasedRegistry;

public interface IPathNavigatorRegistry extends ICallbackBasedRegistry<IPathNavigatorRegistry, IPathNavigatorProducer>
{
    static IPathNavigatorRegistry getInstance()
    {
        return IPerViamInvenireApi.getInstance().getPathNavigateRegistry();
    }
}
