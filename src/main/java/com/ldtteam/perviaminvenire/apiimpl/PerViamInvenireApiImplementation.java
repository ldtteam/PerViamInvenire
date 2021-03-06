package com.ldtteam.perviaminvenire.apiimpl;

import com.ldtteam.perviaminvenire.api.IPerViamInvenireApi;
import com.ldtteam.perviaminvenire.api.adapters.registry.IDismountCartRegistry;
import com.ldtteam.perviaminvenire.api.adapters.registry.IPassableBlockRegistry;
import com.ldtteam.perviaminvenire.api.adapters.registry.IRidingOnCartRegistry;
import com.ldtteam.perviaminvenire.api.adapters.registry.IRoadBlockRegistry;
import com.ldtteam.perviaminvenire.api.adapters.registry.ISpeedAdaptationRegistry;
import com.ldtteam.perviaminvenire.api.adapters.registry.IStartPositionAdapterRegistry;
import com.ldtteam.perviaminvenire.api.adapters.registry.IWalkableBlockRegistry;
import com.ldtteam.perviaminvenire.api.collisions.ICollisionDetectionManager;
import com.ldtteam.perviaminvenire.api.config.ICommonConfig;
import com.ldtteam.perviaminvenire.api.pathfinding.ICalculationResultTracker;
import com.ldtteam.perviaminvenire.api.pathfinding.IPathingResultHandler;
import com.ldtteam.perviaminvenire.api.pathfinding.registry.IPathNavigatorRegistry;
import com.ldtteam.perviaminvenire.collisions.CollisionDetectionManager;
import com.ldtteam.perviaminvenire.config.ConfigurationManager;
import com.ldtteam.perviaminvenire.pathfinding.CalculationResultTracker;
import com.ldtteam.perviaminvenire.pathfinding.PathingResultHandler;
import com.ldtteam.perviaminvenire.pathfinding.registry.DismountCartRegistry;
import com.ldtteam.perviaminvenire.pathfinding.registry.PassableBlockRegistry;
import com.ldtteam.perviaminvenire.pathfinding.registry.PathNavigatorRegistry;
import com.ldtteam.perviaminvenire.pathfinding.registry.RidingOnCartRegistry;
import com.ldtteam.perviaminvenire.pathfinding.registry.RoadBlockRegistry;
import com.ldtteam.perviaminvenire.pathfinding.registry.SpeedAdaptationRegistry;
import com.ldtteam.perviaminvenire.pathfinding.registry.StartPositionAdapterRegistry;
import com.ldtteam.perviaminvenire.pathfinding.registry.WalkableBlockRegistry;

public class PerViamInvenireApiImplementation implements IPerViamInvenireApi {

    @Override
    public IPathNavigatorRegistry getPathNavigateRegistry() {
        return PathNavigatorRegistry.getInstance();
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
}
