package com.ldtteam.perviaminvenire.api.pathfinding;

import com.ldtteam.perviaminvenire.api.IPerViamInvenireApi;
import net.minecraft.server.level.ServerLevel;

/**
 * Describes a type which can render the results of a pathing calculation in the world.
 */
public interface ICalculationResultRenderer
{
    static ICalculationResultRenderer getInstance()
    {
        return IPerViamInvenireApi.getInstance().getCalculationResultRenderer();
    }

    /**
     * Renders the given calculation data into the given world.
     *
     * @param data The calculation data to render into the given world.
     * @param world The world to render the calculation data into.
     */
    void renderDataIntoWorld(PathingCalculationData data, ServerLevel world);
}
