package com.ldtteam.perviaminvenire.api.adapters.registry;

import com.ldtteam.perviaminvenire.api.IPerViamInvenireApi;
import com.ldtteam.perviaminvenire.api.adapters.boundingbox.IBoundingBoxProducer;
import com.ldtteam.perviaminvenire.api.util.ICallbackBasedRegistry;

public interface IBoundingBoxProducerRegistry extends ICallbackBasedRegistry<IBoundingBoxProducerRegistry, IBoundingBoxProducer>
{
    static IBoundingBoxProducerRegistry getInstance() {
        return IPerViamInvenireApi.getInstance().getBoundingBoxRegistry();
    }
}