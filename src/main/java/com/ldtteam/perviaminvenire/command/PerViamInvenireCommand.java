package com.ldtteam.perviaminvenire.command;

import com.ldtteam.perviaminvenire.api.pathfinding.AbstractAdvancedGroundPathNavigator;
import com.ldtteam.perviaminvenire.api.pathfinding.ICalculationResultTracker;
import com.ldtteam.perviaminvenire.api.results.ICalculationResultsImportManager;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.server.ServerWorld;

import java.util.Collection;

public class PerViamInvenireCommand
{
    private static final PerViamInvenireCommand INSTANCE = new PerViamInvenireCommand();

    public static PerViamInvenireCommand getInstance()
    {
        return INSTANCE;
    }

    private PerViamInvenireCommand()
    {
    }

    public void register(final CommandDispatcher<CommandSource> dispatcher)
    {
        dispatcher.register(Commands.literal("pvi").requires(source -> source.hasPermissionLevel(2))
                              .then(Commands.literal("tracking")
                                      .then(Commands.literal("start")
                                              .then(Commands.argument("players", EntityArgument.players())
                                                      .then(Commands.argument("entities", EntityArgument.entities()).executes(source -> {
                                                          final Collection<ServerPlayerEntity> players = EntityArgument.getPlayers(source, "players");
                                                          final Collection<? extends Entity> entities = EntityArgument.getEntitiesAllowingNone(source, "entities");

                                                          int addedTrackingEntries = 0;

                                                          for (final ServerPlayerEntity player : players)
                                                          {
                                                              for (final Entity entity : entities)
                                                              {
                                                                  if (entity instanceof MobEntity)
                                                                  {
                                                                      final MobEntity mobEntity = (MobEntity) entity;
                                                                      if (mobEntity.getNavigator() instanceof AbstractAdvancedGroundPathNavigator)
                                                                      {
                                                                          ICalculationResultTracker.getInstance().startTracking(player, entity);
                                                                          addedTrackingEntries++;
                                                                      }
                                                                  }
                                                              }
                                                          }

                                                          return addedTrackingEntries;
                                                      }))))
                                      .then(Commands.literal("stop")
                                              .then(Commands.argument("players", EntityArgument.players())
                                                      .then(Commands.argument("entities", EntityArgument.entities()).executes(source -> {
                                                          final Collection<ServerPlayerEntity> players = EntityArgument.getPlayers(source, "players");
                                                          final Collection<? extends Entity> entities = EntityArgument.getEntitiesAllowingNone(source, "entities");

                                                          int removedTrackingEntities = 0;

                                                          for (final ServerPlayerEntity player : players)
                                                          {
                                                              for (final Entity entity : entities)
                                                              {
                                                                  if (entity instanceof MobEntity)
                                                                  {
                                                                      final MobEntity mobEntity = (MobEntity) entity;
                                                                      if (mobEntity.getNavigator() instanceof AbstractAdvancedGroundPathNavigator)
                                                                      {
                                                                          ICalculationResultTracker.getInstance().stopTracking(player, entity);
                                                                          removedTrackingEntities++;
                                                                      }
                                                                  }
                                                              }
                                                          }

                                                          return removedTrackingEntities;
                                                      })))))
                              .then(Commands.literal("import")
                                      .then(Commands.literal("start")
                                                      .then(Commands.argument("file", ImportableResultDataArgument.getInstance()).executes(source -> {
                                                          final String identifier = source.getArgument("file", String.class);
                                                          final ServerWorld world = source.getSource().getWorld();

                                                          ICalculationResultsImportManager.getInstance().startRenderingIn(world, identifier);

                                                          return 1;
                                                      })))
                                      .then(Commands.literal("stop")
                                              .then(Commands.argument("file", ImportableResultDataArgument.getInstance()).executes(source -> {
                                                          final String identifier = source.getArgument("file", String.class);
                                                          final ServerWorld world = source.getSource().getWorld();

                                                          ICalculationResultsImportManager.getInstance().stopRenderingIn(world, identifier);

                                                          return 1;
                                                      }))))
                              .then(Commands.literal("exporting")
                                      .then(Commands.literal("start")
                                              .then(Commands.argument("entities", EntityArgument.entities()).executes(source -> {
                                                  final Collection<? extends Entity> entities = EntityArgument.getEntitiesAllowingNone(source, "entities");

                                                  int addedTrackingEntries = 0;

                                                  for (final Entity entity : entities)
                                                  {
                                                      if (entity instanceof MobEntity)
                                                      {
                                                          final MobEntity mobEntity = (MobEntity) entity;
                                                          if (mobEntity.getNavigator() instanceof AbstractAdvancedGroundPathNavigator)
                                                          {
                                                              ICalculationResultTracker.getInstance().startExporting(entity);
                                                              addedTrackingEntries++;
                                                          }
                                                      }
                                                  }

                                                  return addedTrackingEntries;
                                              })))
                                      .then(Commands.literal("stop")
                                              .then(Commands.argument("entities", EntityArgument.entities()).executes(source -> {
                                                  final Collection<? extends Entity> entities = EntityArgument.getEntitiesAllowingNone(source, "entities");

                                                  int removedTrackingEntities = 0;

                                                  for (final Entity entity : entities)
                                                  {
                                                      if (entity instanceof MobEntity)
                                                      {
                                                          final MobEntity mobEntity = (MobEntity) entity;
                                                          if (mobEntity.getNavigator() instanceof AbstractAdvancedGroundPathNavigator)
                                                          {
                                                              ICalculationResultTracker.getInstance().stopExporting(entity);
                                                              removedTrackingEntities++;
                                                          }
                                                      }
                                                  }

                                                  return removedTrackingEntities;
                                              }))))
        );
    }
}
