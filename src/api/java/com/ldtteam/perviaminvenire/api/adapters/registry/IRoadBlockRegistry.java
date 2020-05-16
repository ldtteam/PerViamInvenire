package com.ldtteam.perviaminvenire.api.adapters.registry;

import com.ldtteam.perviaminvenire.api.IPerViamInvenireApi;
import com.ldtteam.perviaminvenire.api.adapters.road.IRoadBlockCallback;

public interface IRoadBlockRegistry extends ICallbackBasedRegistry<IRoadBlockRegistry, IRoadBlockCallback> {
    static IRoadBlockRegistry getInstance() {
        return IPerViamInvenireApi.getInstance().getRoadBlockRegistry();
    }
}
