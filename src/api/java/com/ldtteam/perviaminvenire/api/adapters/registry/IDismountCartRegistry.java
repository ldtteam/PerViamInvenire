package com.ldtteam.perviaminvenire.api.adapters.registry;

import com.ldtteam.perviaminvenire.api.IPerViamInvenireApi;
import com.ldtteam.perviaminvenire.api.adapters.minecart.IDismountCartCallback;
import com.ldtteam.perviaminvenire.api.adapters.minecart.IRidingOnCartCallback;

public interface IDismountCartRegistry extends ICallbackBasedRegistry<IDismountCartRegistry, IDismountCartCallback> {

    static IDismountCartRegistry getInstance() { return IPerViamInvenireApi.getInstance().getDismountCartRegistry(); }
}
