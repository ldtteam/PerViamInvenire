package com.ldtteam.perviaminvenire.api.results;

import com.ldtteam.perviaminvenire.api.IPerViamInvenireApi;
import com.ldtteam.perviaminvenire.api.pathfinding.PathingCalculationData;

import java.util.List;
import java.util.Optional;

/**
 * Describes systems which can store and load calculation results.
 */
public interface ICalculationResultsStorageManager
{
    static ICalculationResultsStorageManager getInstance()
    {
        return IPerViamInvenireApi.getInstance().getCalculationStorageManager();
    }

    /**
     * Allows for the loading of a given calculation data from a given storage path.
     *
     * @param resultsIdentifier The name or identifier of the pathing calculation data.
     * @return An optional potentially containing the calculation data.
     */
    Optional<PathingCalculationData> loadData(String resultsIdentifier);

    /**
     * Allows for the storing of a given calculation data into the storage which is identified
     * with the given name or id.
     *
     * @param data The data to store.
     * @param resultsIdentifier The name or the id to store the data into.
     */
    void storeData(PathingCalculationData data, String resultsIdentifier);

    /**
     * Gives access to all available identifiers that are contained in this storage.
     * @return A list with available identifiers.
     */
    List<String> getAvailableIdentifiers();
}
