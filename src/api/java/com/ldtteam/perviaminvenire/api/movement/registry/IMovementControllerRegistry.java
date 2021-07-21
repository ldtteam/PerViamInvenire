package com.ldtteam.perviaminvenire.api.movement.registry;

import com.ldtteam.perviaminvenire.api.IPerViamInvenireApi;
import com.ldtteam.perviaminvenire.api.movement.IMovementControllerProducer;
import com.ldtteam.perviaminvenire.api.pathfinding.IPathNavigatorProducer;
import com.ldtteam.perviaminvenire.api.util.ICallbackBasedRegistry;

public interface IMovementControllerRegistry extends ICallbackBasedRegistry<IMovementControllerRegistry, IMovementControllerProducer>
{
    static IMovementControllerRegistry getInstance()
    {
        return IPerViamInvenireApi.getInstance().getMovementControllerRegistry();
    }
}
