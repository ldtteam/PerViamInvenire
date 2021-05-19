package com.ldtteam.perviaminvenire.api.adapters.registry;

import com.ldtteam.perviaminvenire.api.IPerViamInvenireApi;
import com.ldtteam.perviaminvenire.api.adapters.ladder.IIsLadderBlockCallback;
import com.ldtteam.perviaminvenire.api.util.ICallbackBasedRegistry;

public interface IIsLadderBlockRegistry extends ICallbackBasedRegistry<IIsLadderBlockRegistry, IIsLadderBlockCallback>
{
    static IIsLadderBlockRegistry getInstance() {
        return IPerViamInvenireApi.getInstance().getLadderBlockRegistry();
    }
}
