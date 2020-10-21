package com.ldtteam.perviaminvenire.api.config;

import com.ldtteam.perviaminvenire.api.IPerViamInvenireApi;

public interface ICommonConfig {

    static ICommonConfig getInstance() {
        return IPerViamInvenireApi.getInstance().getCommonConfig();
    }

    int getMinimumRailsToUseInPath();

    int getMaxPathFindingNodes();

    int getPathFindingLogVerbosity();

    int getPathFindingThreadingCount();
}
