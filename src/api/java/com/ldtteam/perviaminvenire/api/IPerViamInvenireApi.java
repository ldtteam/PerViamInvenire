package com.ldtteam.perviaminvenire.api;

import com.ldtteam.perviaminvenire.api.adapters.registry.*;
import com.ldtteam.perviaminvenire.api.collisions.ICollisionDetectionManager;
import com.ldtteam.perviaminvenire.api.config.ICommonConfig;
import com.ldtteam.perviaminvenire.api.movement.registry.IMovementControllerRegistry;
import com.ldtteam.perviaminvenire.api.pathfinding.ICalculationResultRenderer;
import com.ldtteam.perviaminvenire.api.pathfinding.ICalculationResultTracker;
import com.ldtteam.perviaminvenire.api.pathfinding.IPathingResultHandler;
import com.ldtteam.perviaminvenire.api.pathfinding.registry.IPathNavigatorRegistry;
import com.ldtteam.perviaminvenire.api.results.ICalculationResultsImportManager;
import com.ldtteam.perviaminvenire.api.results.ICalculationResultsStorageManager;

public interface IPerViamInvenireApi
{

    static IPerViamInvenireApi getInstance() {
        return PerViamInvenireApiProxy.getInstance();
    }

    IPathNavigatorRegistry getPathNavigateRegistry();

    IMovementControllerRegistry getMovementControllerRegistry();

    IStartPositionAdapterRegistry getStartPositionAdapterRegistry();

    IRoadBlockRegistry getRoadBlockRegistry();

    IPassableBlockRegistry getPassableBlockRegistry();

    IWalkableBlockRegistry getWalkableBlockRegistry();

    ISpeedAdaptationRegistry getSpeedAdaptationRegistry();

    IRidingOnCartRegistry getRidingOnCartRegistry();

    IDismountCartRegistry getDismountCartRegistry();

    ICalculationResultTracker getResultTracker();

    IPathingResultHandler getResultHandler();

    ICollisionDetectionManager getCollisionDetectionManager();

    ICommonConfig getCommonConfig();

    IIsLadderBlockRegistry getLadderBlockRegistry();

    IBoundingBoxProducerRegistry getBoundingBoxRegistry();

    ICalculationResultRenderer getCalculationResultRenderer();

    ICalculationResultsStorageManager getCalculationStorageManager();

    ICalculationResultsImportManager getCalculationResultsImportManager();
}
