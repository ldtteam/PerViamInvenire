package com.ldtteam.perviaminvenire.api;

import com.ldtteam.perviaminvenire.api.adapters.registry.IDismountCartRegistry;
import com.ldtteam.perviaminvenire.api.adapters.registry.IPassableBlockRegistry;
import com.ldtteam.perviaminvenire.api.adapters.registry.IRidingOnCartRegistry;
import com.ldtteam.perviaminvenire.api.adapters.registry.IRoadBlockRegistry;
import com.ldtteam.perviaminvenire.api.adapters.registry.ISpeedAdaptationRegistry;
import com.ldtteam.perviaminvenire.api.adapters.registry.IStartPositionAdapterRegistry;
import com.ldtteam.perviaminvenire.api.adapters.registry.IWalkableBlockRegistry;
import com.ldtteam.perviaminvenire.api.config.ICommonConfig;
import com.ldtteam.perviaminvenire.api.pathfinding.registry.IPathNavigateRegistry;

public final class PerViamInvenireApiProxy implements IPerViamInvenireApi
{
    private static PerViamInvenireApiProxy ourInstance = new PerViamInvenireApiProxy();

    private IPerViamInvenireApi apiInstance;

    public static PerViamInvenireApiProxy getInstance()
    {
        return ourInstance;
    }

    private PerViamInvenireApiProxy()
    {
    }

    public void setApiInstance(final IPerViamInvenireApi apiInstance)
    {
        this.apiInstance = apiInstance;
    }

    @Override
    public IPathNavigateRegistry getPathNavigateRegistry()
    {
        return apiInstance.getPathNavigateRegistry();
    }

    @Override
    public IStartPositionAdapterRegistry getStartPositionAdapterRegistry() {
        return this.apiInstance.getStartPositionAdapterRegistry();
    }

    @Override
    public IRoadBlockRegistry getRoadBlockRegistry() {
        return this.apiInstance.getRoadBlockRegistry();
    }

    @Override
    public IPassableBlockRegistry getPassableBlockRegistry() {
        return this.apiInstance.getPassableBlockRegistry();
    }

    @Override
    public IWalkableBlockRegistry getWalkableBlockRegistry() {
        return this.apiInstance.getWalkableBlockRegistry();
    }

    @Override
    public ISpeedAdaptationRegistry getSpeedAdaptationRegistry() {
        return this.apiInstance.getSpeedAdaptationRegistry();
    }

    @Override
    public IRidingOnCartRegistry getRidingOnCartRegistry() {
        return this.apiInstance.getRidingOnCartRegistry();
    }

    @Override
    public IDismountCartRegistry getDismountCartRegistry() {
        return this.apiInstance.getDismountCartRegistry();
    }

    @Override
    public ICommonConfig getCommonConfig() {
        return this.apiInstance.getCommonConfig();
    }
}
