package com.ldtteam.perviaminvenire.command;

import com.ldtteam.perviaminvenire.api.pathfinding.AbstractAdvancedGroundPathNavigator;
import com.ldtteam.perviaminvenire.api.pathfinding.IAdvancedPathNavigator;
import com.ldtteam.perviaminvenire.api.pathfinding.ICalculationResultTracker;
import com.ldtteam.perviaminvenire.api.results.ICalculationResultsImportManager;
import com.ldtteam.perviaminvenire.pathfinding.PerViamInvenireGroundPathNavigator;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;

import java.util.Collection;

public class PerViamInvenireCommand {
    private static final PerViamInvenireCommand INSTANCE = new PerViamInvenireCommand();

    public static PerViamInvenireCommand getInstance() {
        return INSTANCE;
    }

    private PerViamInvenireCommand() {
    }

    public void register(final CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("pvi").requires(source -> source.hasPermission(2))
                .then(Commands.literal("tracking")
                        .then(Commands.literal("start")
                                .then(Commands.argument("players", EntityArgument.players())
                                        .then(Commands.argument("entities", EntityArgument.entities()).executes(source -> {
                                            final Collection<ServerPlayer> players = EntityArgument.getPlayers(source, "players");
                                            final Collection<? extends Entity> entities = EntityArgument.getOptionalEntities(source, "entities");

                                            int addedTrackingEntries = 0;

                                            for (final ServerPlayer player : players) {
                                                for (final Entity entity : entities) {
                                                    if (entity instanceof final Mob mobEntity) {
                                                        if (mobEntity.getNavigation() instanceof AbstractAdvancedGroundPathNavigator) {
                                                            ICalculationResultTracker.getInstance().startTracking(player, entity);

                                                            mobEntity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 20 * 60));
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
                                            final Collection<ServerPlayer> players = EntityArgument.getPlayers(source, "players");
                                            final Collection<? extends Entity> entities = EntityArgument.getOptionalEntities(source, "entities");

                                            int removedTrackingEntities = 0;

                                            for (final ServerPlayer player : players) {
                                                for (final Entity entity : entities) {
                                                    if (entity instanceof final Mob mobEntity) {
                                                        if (mobEntity.getNavigation() instanceof AbstractAdvancedGroundPathNavigator) {
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
                                    final ServerLevel world = source.getSource().getLevel();

                                    ICalculationResultsImportManager.getInstance().startRenderingIn(world, identifier);

                                    return 1;
                                })))
                        .then(Commands.literal("stop")
                                .then(Commands.argument("file", ImportableResultDataArgument.getInstance()).executes(source -> {
                                    final String identifier = source.getArgument("file", String.class);
                                    final ServerLevel world = source.getSource().getLevel();

                                    ICalculationResultsImportManager.getInstance().stopRenderingIn(world, identifier);

                                    return 1;
                                }))))
                .then(Commands.literal("exporting")
                        .then(Commands.literal("start")
                                .then(Commands.argument("entities", EntityArgument.entities()).executes(source -> {
                                    final Collection<? extends Entity> entities = EntityArgument.getOptionalEntities(source, "entities");

                                    int addedTrackingEntries = 0;

                                    for (final Entity entity : entities) {
                                        if (entity instanceof final Mob mobEntity) {
                                            if (mobEntity.getNavigation() instanceof AbstractAdvancedGroundPathNavigator) {
                                                ICalculationResultTracker.getInstance().startExporting(entity);
                                                addedTrackingEntries++;
                                            }
                                        }
                                    }

                                    return addedTrackingEntries;
                                })))
                        .then(Commands.literal("stop")
                                .then(Commands.argument("entities", EntityArgument.entities()).executes(source -> {
                                    final Collection<? extends Entity> entities = EntityArgument.getOptionalEntities(source, "entities");

                                    int removedTrackingEntities = 0;

                                    for (final Entity entity : entities) {
                                        if (entity instanceof final Mob mobEntity) {
                                            if (mobEntity.getNavigation() instanceof AbstractAdvancedGroundPathNavigator) {
                                                ICalculationResultTracker.getInstance().stopExporting(entity);
                                                removedTrackingEntities++;
                                            }
                                        }
                                    }

                                    return removedTrackingEntities;
                                }))))
                .then(Commands.literal("move")
                        .then(Commands.argument("entities", EntityArgument.entities())
                                .then(Commands.argument("target", BlockPosArgument.blockPos())
                                        .executes(source -> {
                                            final BlockPos target = BlockPosArgument.getLoadedBlockPos(source, "target");
                                            final Collection<? extends Entity> entities = EntityArgument.getEntities(source, "entities");

                                            for (Entity entity : entities) {
                                                if (entity instanceof Mob mob) {
                                                    if(mob.getNavigation() instanceof IAdvancedPathNavigator advancedPathNavigator) {
                                                        advancedPathNavigator.moveTo(target);
                                                        mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, 20 * 60));
                                                    }
                                                }
                                            }

                                            return 0;
                                        }))))
        );
    }
}
