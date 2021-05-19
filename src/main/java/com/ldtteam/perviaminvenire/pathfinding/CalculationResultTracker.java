package com.ldtteam.perviaminvenire.pathfinding;

import com.google.common.collect.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ldtteam.perviaminvenire.api.pathfinding.AbstractPathJob;
import com.ldtteam.perviaminvenire.api.pathfinding.ICalculationResultTracker;
import com.ldtteam.perviaminvenire.api.pathfinding.PathingCalculationData;
import com.ldtteam.perviaminvenire.api.results.ICalculationResultsStorageManager;
import com.ldtteam.perviaminvenire.network.NetworkManager;
import com.ldtteam.perviaminvenire.network.message.OnCalculationCompleted;
import com.ldtteam.perviaminvenire.results.CalculationResultsStorageManager;
import com.ldtteam.perviaminvenire.util.gson.MultimapAdapter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class CalculationResultTracker implements ICalculationResultTracker
{
    private static final ICalculationResultTracker INSTANCE = new CalculationResultTracker();

    public static ICalculationResultTracker getInstance()
    {
        return INSTANCE;
    }

    private final Multimap<PlayerEntity, Entity> playerToTrackingEntity = Multimaps.synchronizedMultimap(HashMultimap.create());
    private final Multimap<Entity, PlayerEntity> entityToTrackingPlayer = Multimaps.synchronizedMultimap(HashMultimap.create());
    private final Set<Entity> entitiesToExport = Sets.newConcurrentHashSet();

    private CalculationResultTracker()
    {
    }

    @Override
    public void onEntityLeaveWorld(Entity entity)
    {
        if (entity instanceof PlayerEntity)
        {
            final PlayerEntity playerEntity = (PlayerEntity) entity;
            playerToTrackingEntity.removeAll(playerEntity);

            entityToTrackingPlayer.entries()
              .stream()
              .filter(e -> e.getValue() == playerEntity)
              .collect(Collectors.toList())
              .forEach(e -> entityToTrackingPlayer.remove(e.getKey(), e.getValue()));
        }
        else {
            entityToTrackingPlayer.removeAll(entity);

            playerToTrackingEntity.entries()
              .stream()
              .filter(e -> e.getValue() == entity)
              .collect(Collectors.toList())
              .forEach(e -> playerToTrackingEntity.remove(e.getKey(), e.getValue()));
        }
    }

    @Override
    public void startTracking(final PlayerEntity playerEntity, final Entity entity) {
        playerToTrackingEntity.put(playerEntity, entity);
        entityToTrackingPlayer.put(entity, playerEntity);
    }

    @Override
    public void stopTracking(final PlayerEntity playerEntity, final Entity entity) {
        playerToTrackingEntity.remove(playerEntity, entity);
        entityToTrackingPlayer.remove(entity, playerEntity);
    }

    @Override
    public void startExporting(final Entity entity)
    {
        entitiesToExport.add(entity);
    }

    @Override
    public void stopExporting(final Entity entity)
    {
        entitiesToExport.remove(entity);
    }

    @Override
    public void onCalculationCompleted(final AbstractPathJob job) {
        final Entity entity = job.getEntity();
        if (entity == null)
            return; //Entity got unloaded. Skipping.

        final PathingCalculationData data = job.getCalculationData();

        NetworkManager.getInstance().sendToPlayer(
          new OnCalculationCompleted(entity.getUniqueID(), data),
          entityToTrackingPlayer.get(entity)
            .stream()
            .filter(ServerPlayerEntity.class::isInstance)
            .map(ServerPlayerEntity.class::cast).toArray(ServerPlayerEntity[]::new)
        );

        final String storagePathName = entity.getUniqueID().toString();
        if (entitiesToExport.contains(job.getEntity()))
        {
            ICalculationResultsStorageManager.getInstance().storeData(data, storagePathName);
        }
    }


    @Override
    public Collection<PlayerEntity> getTrackingPlayers(final Entity entity)
    {
        return entityToTrackingPlayer.get(entity);
    }
}
