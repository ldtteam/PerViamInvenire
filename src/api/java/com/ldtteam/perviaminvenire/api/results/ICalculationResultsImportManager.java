package com.ldtteam.perviaminvenire.api.results;

import com.ldtteam.perviaminvenire.api.IPerViamInvenireApi;
import net.minecraft.server.level.ServerLevel;

/**
 * Describes systems which can import and render imported
 * calculations results.
 */
public interface ICalculationResultsImportManager
{

    static ICalculationResultsImportManager getInstance()
    {
        return IPerViamInvenireApi.getInstance().getCalculationResultsImportManager();
    }

    /**
     * Invoke this to check if the imported paths should be
     * rendered in a given world.
     *
     * @param world The world to check and potentially render in.
     */
    void onPostWorldTick(ServerLevel world);

    /**
     * Indicates that the results with the given identifier potentially should be loaded
     * and that they should be rendered in the given world.
     *
     * @param world The world to render the imported results into.
     * @param resultsIdentifier The identifier of the given results.
     */
    void startRenderingIn(ServerLevel world, String resultsIdentifier);

    /**
     * Indicates that the results with the given identifier should not be rendered
     * in the given world. Also unloads the given calculation data if it is not
     * rendered in any other world anymore.
     *
     * @param world The world to stop rendering the imported results in.
     * @param resultsIdentifier The identifier of the given results.
     */
    void stopRenderingIn(ServerLevel world, String resultsIdentifier);
}
