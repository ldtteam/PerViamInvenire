package com.ldtteam.perviaminvenire.api;

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
}
