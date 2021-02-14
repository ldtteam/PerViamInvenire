package com.ldtteam.perviaminvenire.api.adapters.registry;

import com.ldtteam.perviaminvenire.api.IPerViamInvenireApi;
import com.ldtteam.perviaminvenire.api.adapters.road.IRoadBlockCallback;
import com.ldtteam.perviaminvenire.api.util.ICallbackBasedRegistry;

public interface IRoadBlockRegistry extends ICallbackBasedRegistry<IRoadBlockRegistry, IRoadBlockCallback>
{
    static IRoadBlockRegistry getInstance() {
        return IPerViamInvenireApi.getInstance().getRoadBlockRegistry();
    }
}
