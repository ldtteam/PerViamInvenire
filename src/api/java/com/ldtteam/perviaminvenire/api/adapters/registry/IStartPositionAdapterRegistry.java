package com.ldtteam.perviaminvenire.api.adapters.registry;

import com.ldtteam.perviaminvenire.api.IPerViamInvenireApi;
import com.ldtteam.perviaminvenire.api.adapters.start.IStartPositionAdapter;

/**
 * Registry used to register adapters for start positions.
 */
public interface IStartPositionAdapterRegistry extends ICallbackBasedRegistry<IStartPositionAdapterRegistry, IStartPositionAdapter> {
    static IStartPositionAdapterRegistry getInstance() {
        return IPerViamInvenireApi.getInstance().getStartPositionAdapterRegistry();
    }
}
