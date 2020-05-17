package com.ldtteam.perviaminvenire.api;

import com.ldtteam.perviaminvenire.api.adapters.minecart.IDismountCartCallback;
import com.ldtteam.perviaminvenire.api.adapters.registry.IDismountCartRegistry;
import com.ldtteam.perviaminvenire.api.adapters.registry.IPassableBlockRegistry;
import com.ldtteam.perviaminvenire.api.adapters.registry.IRidingOnCartRegistry;
import com.ldtteam.perviaminvenire.api.adapters.registry.IRoadBlockRegistry;
import com.ldtteam.perviaminvenire.api.adapters.registry.ISpeedAdaptationRegistry;
import com.ldtteam.perviaminvenire.api.adapters.registry.IWalkableBlockRegistry;
import com.ldtteam.perviaminvenire.api.config.ICommonConfig;
import com.ldtteam.perviaminvenire.api.pathfinding.registry.IPathNavigateRegistry;
import com.ldtteam.perviaminvenire.api.adapters.registry.IStartPositionAdapterRegistry;

public interface IPerViamInvenireApi
{

    static IPerViamInvenireApi getInstance() {
        return PerViamInvenireApiProxy.getInstance();
    }

    IPathNavigateRegistry getPathNavigateRegistry();

    IStartPositionAdapterRegistry getStartPositionAdapterRegistry();

    IRoadBlockRegistry getRoadBlockRegistry();

    IPassableBlockRegistry getPassableBlockRegistry();

    IWalkableBlockRegistry getWalkableBlockRegistry();

    ISpeedAdaptationRegistry getSpeedAdaptationRegistry();

    IRidingOnCartRegistry getRidingOnCartRegistry();

    IDismountCartRegistry getDismountCartRegistry();

    ICommonConfig getCommonConfig();
}
