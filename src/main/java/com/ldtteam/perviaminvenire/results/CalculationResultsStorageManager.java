package com.ldtteam.perviaminvenire.results;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.ldtteam.perviaminvenire.api.pathfinding.PathingCalculationData;
import com.ldtteam.perviaminvenire.api.results.ICalculationResultsStorageManager;
import com.ldtteam.perviaminvenire.util.gson.MultimapAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

public class CalculationResultsStorageManager implements ICalculationResultsStorageManager
{
    private static final CalculationResultsStorageManager INSTANCE = new CalculationResultsStorageManager();
    private static final Logger LOGGER = LogManager.getLogger();

    public static CalculationResultsStorageManager getInstance()
    {
        return INSTANCE;
    }

    private final Gson gson = new GsonBuilder().setPrettyPrinting().setLenient()
                                .registerTypeAdapter(Multimap.class, new MultimapAdapter())
                                .enableComplexMapKeySerialization()
                                .create();

    private CalculationResultsStorageManager()
    {
    }

    @Override
    public Optional<PathingCalculationData> loadData(final String resultsIdentifier)
    {
        Path path = buildStoragePath(resultsIdentifier);
        if (!path.toFile().exists())
        {
            LOGGER.warn(String.format("Tried to load a pathing calculation data from an not existing storage location: %s", path));
            return Optional.empty();
        }

        try
        {
            JsonReader reader = new JsonReader(new FileReader(path.toAbsolutePath().toFile()));
            return Optional.of(gson.fromJson(reader, PathingCalculationData.class));
        }
        catch (IOException e)
        {
            LOGGER.error(String.format("Failed to load the calculation data to: %s", path), e);
            return Optional.empty();
        }
    }

    @Override
    public void storeData(final PathingCalculationData data, final String resultsIdentifier)
    {
        Path path = buildStoragePath(resultsIdentifier);
        try
        {
            if (path.toFile().getParentFile().mkdirs())
                LOGGER.info(String.format("Created PVI storage directory at: %s", path.toFile().getParentFile().getAbsolutePath()));

            Files.deleteIfExists(path);
            Files.write(path, Lists.newArrayList(gson.toJson(data)), StandardOpenOption.CREATE_NEW);
        }
        catch (IOException e)
        {
            LOGGER.error(String.format("Failed to write the calculation data to: %s", path), e);
        }
    }

    @Override
    public List<String> getAvailableIdentifiers()
    {
        final File pviPathsDirectory = new File("./pvi-paths");
        if (!pviPathsDirectory.exists())
            return Lists.newArrayList();

        final String[] validFileNames = pviPathsDirectory.list((dir, name) -> name.endsWith(".json"));

        return validFileNames == null ? Collections.emptyList() : Arrays.stream(validFileNames).map(fileName -> fileName.replace(".json", "")).collect(Collectors.toList());
    }

    @NotNull
    private Path buildStoragePath(final String storagePathName)
    {
        return Paths.get(String.format("./pvi-paths/%s.json", storagePathName)).toAbsolutePath();
    }
}
