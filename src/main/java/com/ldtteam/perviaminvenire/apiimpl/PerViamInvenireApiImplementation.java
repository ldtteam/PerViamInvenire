package com.ldtteam.perviaminvenire.apiimpl;

import com.ldtteam.perviaminvenire.api.IPerViamInvenireApi;
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
import com.ldtteam.perviaminvenire.collisions.CollisionDetectionManager;
import com.ldtteam.perviaminvenire.config.ConfigurationManager;
import com.ldtteam.perviaminvenire.movement.registry.WantedMovementHandlerRegistry;
import com.ldtteam.perviaminvenire.pathfinding.CalculationResultRenderer;
import com.ldtteam.perviaminvenire.pathfinding.CalculationResultTracker;
import com.ldtteam.perviaminvenire.pathfinding.PathingResultHandler;
import com.ldtteam.perviaminvenire.pathfinding.registry.*;
import com.ldtteam.perviaminvenire.results.CalculationResultsImportManager;
import com.ldtteam.perviaminvenire.results.CalculationResultsStorageManager;

public class PerViamInvenireApiImplementation implements IPerViamInvenireApi {

    @Override
    public IPathNavigatorRegistry getPathNavigateRegistry() {
        return PathNavigatorRegistry.getInstance();
    }

    @Override
    public IMovementControllerRegistry getMovementControllerRegistry()
    {
        return MovementControllerRegistry.getInstance();
    }

    @Override
    public IStartPositionAdapterRegistry getStartPositionAdapterRegistry() {
        return StartPositionAdapterRegistry.getInstance();
    }

    @Override
    public IRoadBlockRegistry getRoadBlockRegistry() {
        return RoadBlockRegistry.getInstance();
    }

    @Override
    public IPassableBlockRegistry getPassableBlockRegistry() {
        return PassableBlockRegistry.getInstance();
    }

    @Override
    public IWalkableBlockRegistry getWalkableBlockRegistry() {
        return WalkableBlockRegistry.getInstance();
    }

    @Override
    public ISpeedAdaptationRegistry getSpeedAdaptationRegistry() {
        return SpeedAdaptationRegistry.getInstance();
    }

    @Override
    public IRidingOnCartRegistry getRidingOnCartRegistry() {
        return RidingOnCartRegistry.getInstance();
    }

    @Override
    public IDismountCartRegistry getDismountCartRegistry() {
        return DismountCartRegistry.getInstance();
    }

    @Override
    public ICalculationResultTracker getResultTracker()
    {
        return CalculationResultTracker.getInstance();
    }

    @Override
    public IPathingResultHandler getResultHandler()
    {
        return PathingResultHandler.getInstance();
    }

    @Override
    public ICollisionDetectionManager getCollisionDetectionManager()
    {
        return CollisionDetectionManager.getInstance();
    }

    @Override
    public ICommonConfig getCommonConfig() {
        return ConfigurationManager.getInstance().getCommonConfig();
    }

    @Override
    public IIsLadderBlockRegistry getLadderBlockRegistry()
    {
        return IsLadderBlockRegistry.getInstance();
    }

    @Override
    public IBoundingBoxProducerRegistry getBoundingBoxRegistry()
    {
        return BoundingBoxProducerRegistry.getInstance();
    }

    @Override
    public ICalculationResultRenderer getCalculationResultRenderer()
    {
        return CalculationResultRenderer.getInstance();
    }

    @Override
    public ICalculationResultsStorageManager getCalculationStorageManager()
    {
        return CalculationResultsStorageManager.getInstance();
    }

    @Override
    public ICalculationResultsImportManager getCalculationResultsImportManager()
    {
        return CalculationResultsImportManager.getInstance();
    }

    @Override
    public IWantedMovementHandlerRegistry getWantedMovementHandlerRegistry() {
        return WantedMovementHandlerRegistry.getInstance();
    }
}
