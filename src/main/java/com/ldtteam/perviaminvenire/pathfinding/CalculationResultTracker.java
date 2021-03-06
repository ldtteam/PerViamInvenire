package com.ldtteam.perviaminvenire.pathfinding;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.ldtteam.perviaminvenire.api.pathfinding.AbstractPathJob;
import com.ldtteam.perviaminvenire.api.pathfinding.ICalculationResultTracker;
import com.ldtteam.perviaminvenire.api.pathfinding.PathingCalculationData;
import com.ldtteam.perviaminvenire.network.NetworkManager;
import com.ldtteam.perviaminvenire.network.message.OnCalculationCompleted;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.Collection;
import java.util.stream.Collectors;

public class CalculationResultTracker implements ICalculationResultTracker
{
    private static final ICalculationResultTracker INSTANCE = new CalculationResultTracker();

    public static ICalculationResultTracker getInstance()
    {
        return INSTANCE;
    }

    private final Multimap<PlayerEntity, Entity> playerToTrackingEntity = HashMultimap.create();
    private final Multimap<Entity, PlayerEntity> entityToTrackingPlayer = HashMultimap.create();

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
    }

    @Override
    public Collection<PlayerEntity> getTrackingPlayers(final Entity entity)
    {
        return entityToTrackingPlayer.get(entity);
    }
}
