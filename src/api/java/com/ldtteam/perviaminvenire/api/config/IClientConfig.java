package com.ldtteam.perviaminvenire.api.config;

import com.ldtteam.perviaminvenire.api.IPerViamInvenireApi;

public interface IClientConfig extends ICommonConfig {

    static IClientConfig getInstance() {
        return IPerViamInvenireApi.getInstance().getClientConfig();
    }

    boolean shouldDoDebugDrawing();
}
