package com.ldtteam.perviaminvenire.api.movement.registry;

import com.ldtteam.perviaminvenire.api.IPerViamInvenireApi;
import com.ldtteam.perviaminvenire.api.adapters.movement.IWantedMovementHandler;
import com.ldtteam.perviaminvenire.api.util.ICallbackBasedRegistry;

public interface IWantedMovementHandlerRegistry extends ICallbackBasedRegistry<IWantedMovementHandlerRegistry, IWantedMovementHandler>
{
    static IWantedMovementHandlerRegistry getInstance() {
        return IPerViamInvenireApi.getInstance().getWantedMovementHandlerRegistry();
    }
}
