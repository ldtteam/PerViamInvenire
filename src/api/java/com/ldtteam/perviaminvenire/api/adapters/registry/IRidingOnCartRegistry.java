package com.ldtteam.perviaminvenire.api.adapters.registry;

import com.ldtteam.perviaminvenire.api.IPerViamInvenireApi;
import com.ldtteam.perviaminvenire.api.adapters.minecart.IRidingOnCartCallback;
import com.ldtteam.perviaminvenire.api.util.ICallbackBasedRegistry;

public interface IRidingOnCartRegistry extends ICallbackBasedRegistry<IRidingOnCartRegistry, IRidingOnCartCallback>
{

    static IRidingOnCartRegistry getInstance() { return IPerViamInvenireApi.getInstance().getRidingOnCartRegistry(); }
}
