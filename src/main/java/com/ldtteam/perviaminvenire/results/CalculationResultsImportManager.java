package com.ldtteam.perviaminvenire.results;

import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;
import com.ldtteam.perviaminvenire.api.pathfinding.ICalculationResultRenderer;
import com.ldtteam.perviaminvenire.api.pathfinding.PathingCalculationData;
import com.ldtteam.perviaminvenire.api.results.ICalculationResultsImportManager;
import com.ldtteam.perviaminvenire.api.results.ICalculationResultsStorageManager;
import net.minecraft.world.server.ServerWorld;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CalculationResultsImportManager implements ICalculationResultsImportManager
{
    private static final CalculationResultsImportManager INSTANCE = new CalculationResultsImportManager();

    public static CalculationResultsImportManager getInstance()
    {
        return INSTANCE;
    }

    private final Map<ServerWorld, List<String>> worldToRenderedDataMap = new MapMaker().concurrencyLevel(4)
        .weakKeys()
        .makeMap();
    private final Map<String, PathingCalculationData> idToDataMap = Maps.newConcurrentMap();

    private CalculationResultsImportManager()
    {
    }

    @Override
    public void onPostWorldTick(final ServerWorld world)
    {
        if (!worldToRenderedDataMap.containsKey(world))
            return;

        if (world.getGameTime() % 40 != 0)
            return;

        worldToRenderedDataMap.get(world)
          .stream()
          .filter(idToDataMap::containsKey)
          .map(idToDataMap::get)
          .forEach(data -> ICalculationResultRenderer.getInstance().renderDataIntoWorld(data, world));
    }

    @Override
    public void startRenderingIn(final ServerWorld world, final String resultsIdentifier)
    {
        if (!worldToRenderedDataMap.containsKey(world))
            worldToRenderedDataMap.put(world, Lists.newCopyOnWriteArrayList());

        if (worldToRenderedDataMap.get(world).contains(resultsIdentifier))
            return;

        final Optional<PathingCalculationData> loadedData = ICalculationResultsStorageManager.getInstance().loadData(resultsIdentifier);
        if (loadedData.isPresent())
        {
            if (!idToDataMap.containsKey(resultsIdentifier))
                idToDataMap.put(resultsIdentifier, loadedData.get());

            worldToRenderedDataMap.get(world).add(resultsIdentifier);
        }
    }

    @Override
    public void stopRenderingIn(final ServerWorld world, final String resultsIdentifier)
    {
        if (!worldToRenderedDataMap.containsKey(world))
            return;

        worldToRenderedDataMap.get(world).remove(resultsIdentifier);

        if (worldToRenderedDataMap.values().stream().flatMap(List::stream).noneMatch(resultsIdentifier::equals))
            idToDataMap.remove(resultsIdentifier);
    }
}
