package com.ldtteam.perviaminvenire.api;

import com.ldtteam.perviaminvenire.api.adapters.registry.*;
import com.ldtteam.perviaminvenire.api.collisions.ICollisionDetectionManager;
import com.ldtteam.perviaminvenire.api.config.ICommonConfig;
import com.ldtteam.perviaminvenire.api.movement.registry.IMovementControllerRegistry;
import com.ldtteam.perviaminvenire.api.movement.registry.IWantedMovementHandlerRegistry;
import com.ldtteam.perviaminvenire.api.pathfinding.ICalculationResultRenderer;
import com.ldtteam.perviaminvenire.api.pathfinding.ICalculationResultTracker;
import com.ldtteam.perviaminvenire.api.pathfinding.IPathingResultHandler;
import com.ldtteam.perviaminvenire.api.pathfinding.registry.IPathNavigatorRegistry;
import com.ldtteam.perviaminvenire.api.results.ICalculationResultsImportManager;
import com.ldtteam.perviaminvenire.api.results.ICalculationResultsStorageManager;

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
    public IPathNavigatorRegistry getPathNavigateRegistry()
    {
        return apiInstance.getPathNavigateRegistry();
    }

    @Override
    public IMovementControllerRegistry getMovementControllerRegistry()
    {
        return apiInstance.getMovementControllerRegistry();
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
    public ICollisionDetectionManager getCollisionDetectionManager()
    {
        return this.apiInstance.getCollisionDetectionManager();
    }

    @Override
    public ICalculationResultTracker getResultTracker()
    {
        return this.apiInstance.getResultTracker();
    }

    @Override
    public IPathingResultHandler getResultHandler()
    {
        return this.apiInstance.getResultHandler();
    }

    @Override
    public ICommonConfig getCommonConfig() {
        return this.apiInstance.getCommonConfig();
    }

    @Override
    public IIsLadderBlockRegistry getLadderBlockRegistry()
    {
        return this.apiInstance.getLadderBlockRegistry();
    }

    @Override
    public IBoundingBoxProducerRegistry getBoundingBoxRegistry()
    {
        return this.apiInstance.getBoundingBoxRegistry();
    }

    @Override
    public ICalculationResultRenderer getCalculationResultRenderer()
    {
        return this.apiInstance.getCalculationResultRenderer();
    }

    @Override
    public ICalculationResultsStorageManager getCalculationStorageManager()
    {
        return this.apiInstance.getCalculationStorageManager();
    }

    @Override
    public ICalculationResultsImportManager getCalculationResultsImportManager()
    {
        return this.apiInstance.getCalculationResultsImportManager();
    }

    @Override
    public IWantedMovementHandlerRegistry getWantedMovementHandlerRegistry() {
        return this.apiInstance.getWantedMovementHandlerRegistry();
    }
}
