package com.ldtteam.perviaminvenire.api.adapters.registry;

import com.ldtteam.perviaminvenire.api.IPerViamInvenireApi;
import com.ldtteam.perviaminvenire.api.adapters.speed.ISpeedAdaptationCallback;
import com.ldtteam.perviaminvenire.api.util.ICallbackBasedRegistry;

public interface ISpeedAdaptationRegistry extends ICallbackBasedRegistry<ISpeedAdaptationRegistry, ISpeedAdaptationCallback>
{
    static ISpeedAdaptationRegistry getInstance() { return IPerViamInvenireApi.getInstance().getSpeedAdaptationRegistry(); }
}
