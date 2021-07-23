package com.ldtteam.perviaminvenire.api.pathfinding;

import com.ldtteam.perviaminvenire.api.IPerViamInvenireApi;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;

/**
 * The handler that gets invoked once a pathing result has been determined.
 */
public interface IPathingResultHandler
{

    static IPathingResultHandler getInstance() {
        return IPerViamInvenireApi.getInstance().getResultHandler();
    }

    /**
     * Invoked when a calculation completes.
     * @param data The data holding the calculation result.
     * @param entity The entity for which the calculation completed.
     * @param world The world.
     */
    void onCompleted(
      final PathingCalculationData data,
      final Entity entity,
      final ServerLevel world
    );
}
