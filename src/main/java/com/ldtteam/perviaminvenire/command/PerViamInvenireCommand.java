package com.ldtteam.perviaminvenire.command;

import com.ldtteam.perviaminvenire.api.pathfinding.AbstractAdvancedGroundPathNavigator;
import com.ldtteam.perviaminvenire.api.pathfinding.ICalculationResultTracker;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.ServerPlayerEntity;

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

    public void register(final CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("pvi").requires(source -> source.hasPermissionLevel(2))
            .then(Commands.literal("start").then(Commands.argument("players", EntityArgument.players()).then(Commands.argument("entities", EntityArgument.entities()).executes(source -> {
              final Collection<ServerPlayerEntity> players = EntityArgument.getPlayers(source, "players");
              final Collection<? extends Entity> entities = EntityArgument.getEntitiesAllowingNone(source, "entities");

              int addedTrackingEntries = 0;

              for(final ServerPlayerEntity player : players)
              {
                  for(final Entity entity : entities)
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
            .then(Commands.literal("stop").then(Commands.argument("players", EntityArgument.players()).then(Commands.argument("entities", EntityArgument.entities()).executes(source -> {
                final Collection<ServerPlayerEntity> players = EntityArgument.getPlayers(source, "players");
                final Collection<? extends Entity> entities = EntityArgument.getEntitiesAllowingNone(source, "entities");

                int removedTrackingEntities = 0;

                for(final ServerPlayerEntity player : players)
                {
                    for(final Entity entity : entities)
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
            }))))
          );
    }
}
