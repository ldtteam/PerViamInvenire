package com.ldtteam.perviaminvenire.api;

import com.ldtteam.perviaminvenire.api.adapters.registry.IDismountCartRegistry;
import com.ldtteam.perviaminvenire.api.adapters.registry.IPassableBlockRegistry;
import com.ldtteam.perviaminvenire.api.adapters.registry.IRidingOnCartRegistry;
import com.ldtteam.perviaminvenire.api.adapters.registry.IRoadBlockRegistry;
import com.ldtteam.perviaminvenire.api.adapters.registry.ISpeedAdaptationRegistry;
import com.ldtteam.perviaminvenire.api.adapters.registry.IWalkableBlockRegistry;
import com.ldtteam.perviaminvenire.api.config.ICommonConfig;
import com.ldtteam.perviaminvenire.api.pathfinding.ICalculationResultTracker;
import com.ldtteam.perviaminvenire.api.pathfinding.IPathingResultHandler;
import com.ldtteam.perviaminvenire.api.pathfinding.registry.IPathNavigatorRegistry;
import com.ldtteam.perviaminvenire.api.adapters.registry.IStartPositionAdapterRegistry;

public interface IPerViamInvenireApi
{

    static IPerViamInvenireApi getInstance() {
        return PerViamInvenireApiProxy.getInstance();
    }

    IPathNavigatorRegistry getPathNavigateRegistry();

    IStartPositionAdapterRegistry getStartPositionAdapterRegistry();

    IRoadBlockRegistry getRoadBlockRegistry();

    IPassableBlockRegistry getPassableBlockRegistry();

    IWalkableBlockRegistry getWalkableBlockRegistry();

    ISpeedAdaptationRegistry getSpeedAdaptationRegistry();

    IRidingOnCartRegistry getRidingOnCartRegistry();

    IDismountCartRegistry getDismountCartRegistry();

    ICalculationResultTracker getResultTracker();

    IPathingResultHandler getResultHandler();

    ICommonConfig getCommonConfig();

}
