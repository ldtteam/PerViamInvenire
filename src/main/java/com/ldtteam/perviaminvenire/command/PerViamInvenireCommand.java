package com.ldtteam.perviaminvenire.command;

import com.ldtteam.perviaminvenire.api.pathfinding.AbstractAdvancedGroundPathNavigator;
import com.ldtteam.perviaminvenire.api.pathfinding.IAdvancedPathNavigator;
import com.ldtteam.perviaminvenire.api.pathfinding.ICalculationResultTracker;
import com.ldtteam.perviaminvenire.api.results.ICalculationResultsImportManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.server.command.EnumArgument;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
                .then(Commands.literal("test")
                        .then(Commands.literal("forEntity")
                                .then(Commands.argument("entityId", ResourceLocationArgument.id())
                                        .then(Commands.argument("rotation", EnumArgument.enumArgument(Rotation.class))
                                                .executes(context -> {
                                                    return doRunBatchedTestsFor(
                                                            context,
                                                            ResourceLocationArgument.getId(context, "entityId"),
                                                            context.getArgument("rotation", Rotation.class)
                                                    );
                                                }))))
                        .then(Commands.literal("manual")
                                .then(Commands.argument("entityId", ResourceLocationArgument.id())
                                        .then(Commands.argument("rotation", EnumArgument.enumArgument(Rotation.class))
                                                .executes(context -> {
                                                    return doRunManualTestsFor(
                                                            context,
                                                            ResourceLocationArgument.getId(context, "entityId"),
                                                            context.getArgument("rotation", Rotation.class)
                                                    );
                                                }))))
                        .then(Commands.literal("named")
                                .then(Commands.argument("entityId", ResourceLocationArgument.id())
                                        .then(Commands.argument("batch", StringArgumentType.greedyString())
                                                .executes(context -> {
                                                    return doRunBatchedTests(context, StringArgumentType.getString(context, "batch"), ResourceLocationArgument.getId(context, "entityId"));
                                                }))))
                        .then(Commands.literal("batched")
                                .then(Commands.argument("batch", StringArgumentType.greedyString())
                                        .executes(context -> {
                                            return doRunBatchedTests(context, StringArgumentType.getString(context, "batch"), null);
                                        }))))
        );
    }

    private static int doRunBatchedTests(CommandContext<CommandSourceStack> context, final String batchRegex, final ResourceLocation entityId) {
        final Pattern batchPattern = Pattern.compile(batchRegex);

        GameTestRunner.clearMarkers(context.getSource().getLevel());
        Collection<TestFunction> tests = GameTestRegistry.getAllTestFunctions();

        Collection<TestFunction> testsToRun = tests.parallelStream().filter(test -> batchPattern.matcher(test.getBatchName()).matches() &&
                (entityId == null || test.getTestName().equals(entityId.toString()))).collect(Collectors.toList());

        context.getSource().sendSuccess(Component.literal("Running all " + testsToRun.size() + " tests..."), false);
        GameTestRegistry.forgetFailedTests();

        BlockPos sourcePosition = new BlockPos(context.getSource().getPosition());
        BlockPos startPosition = new BlockPos(sourcePosition.getX(), context.getSource().getLevel().getHeightmapPos(Heightmap.Types.WORLD_SURFACE, sourcePosition).getY(), sourcePosition.getZ() + 3);
        ServerLevel serverlevel = context.getSource().getLevel();
        Rotation rotation = StructureUtils.getRotationForRotationSteps(0);

        Collection<GameTestInfo> results = GameTestRunner.runTests(testsToRun, startPosition, rotation, serverlevel, GameTestTicker.SINGLETON, 8);

        MultipleTestTracker multipletesttracker = new MultipleTestTracker(results);
        multipletesttracker.addListener(new TestSummaryDisplayer(serverlevel, multipletesttracker));
        multipletesttracker.addFailureListener((failedTest) -> GameTestRegistry.rememberFailedTest(failedTest.getTestFunction()));
        return 1;
    }

    private static int doRunBatchedTestsFor(CommandContext<CommandSourceStack> context, final ResourceLocation entityId, final Rotation rotation) {
        GameTestRunner.clearMarkers(context.getSource().getLevel());
        Collection<TestFunction> tests = GameTestRegistry.getAllTestFunctions();

        Collection<TestFunction> testsToRun = tests.parallelStream().filter(test -> test.getTestName().equals(entityId.toString()) && test.getRotation().equals(rotation)).collect(Collectors.toList());

        context.getSource().sendSuccess(Component.literal("Running all " + testsToRun.size() + " tests..."), false);
        GameTestRegistry.forgetFailedTests();

        BlockPos sourcePosition = new BlockPos(context.getSource().getPosition());
        BlockPos startPosition = new BlockPos(sourcePosition.getX(), context.getSource().getLevel().getHeightmapPos(Heightmap.Types.WORLD_SURFACE, sourcePosition).getY(), sourcePosition.getZ() + 3);
        ServerLevel serverlevel = context.getSource().getLevel();

        Collection<GameTestInfo> results = GameTestRunner.runTests(testsToRun, startPosition, rotation, serverlevel, GameTestTicker.SINGLETON, 8);

        MultipleTestTracker multipletesttracker = new MultipleTestTracker(results);
        multipletesttracker.addListener(new TestSummaryDisplayer(serverlevel, multipletesttracker));
        multipletesttracker.addFailureListener((failedTest) -> GameTestRegistry.rememberFailedTest(failedTest.getTestFunction()));
        return 1;
    }


    private static int doRunManualTestsFor(CommandContext<CommandSourceStack> context, final ResourceLocation entityId, final Rotation rotation) {
        GameTestRunner.clearMarkers(context.getSource().getLevel());
        Collection<TestFunction> tests = GameTestRegistry.getAllTestFunctions();

        Collection<TestFunction> testsToRun = tests
                .parallelStream()
                .filter(test -> test.getTestName().equals(entityId.toString()) && test.getRotation().equals(rotation) && test.getBatchName().startsWith("[MANUAL]")).collect(Collectors.toList());

        context.getSource().sendSuccess(Component.literal("Running all " + testsToRun.size() + " tests..."), false);
        GameTestRegistry.forgetFailedTests();

        BlockPos sourcePosition = new BlockPos(context.getSource().getPosition());
        BlockPos startPosition = new BlockPos(sourcePosition.getX(), context.getSource().getLevel().getHeightmapPos(Heightmap.Types.WORLD_SURFACE, sourcePosition).getY(), sourcePosition.getZ() + 3);
        ServerLevel serverlevel = context.getSource().getLevel();

        Collection<GameTestInfo> results = GameTestRunner.runTests(testsToRun, startPosition, rotation, serverlevel, GameTestTicker.SINGLETON, 8);

        MultipleTestTracker multipletesttracker = new MultipleTestTracker(results);
        multipletesttracker.addListener(new TestSummaryDisplayer(serverlevel, multipletesttracker));
        multipletesttracker.addFailureListener((failedTest) -> GameTestRegistry.rememberFailedTest(failedTest.getTestFunction()));
        return 1;
    }

    static class TestSummaryDisplayer implements GameTestListener {
        private final ServerLevel level;
        private final MultipleTestTracker tracker;

        public TestSummaryDisplayer(ServerLevel pServerLevel, MultipleTestTracker pTracker) {
            this.level = pServerLevel;
            this.tracker = pTracker;
        }

        public void testStructureLoaded(@NotNull GameTestInfo pTestInfo) {
        }

        public void testPassed(@NotNull GameTestInfo pTestInfo) {
            showTestSummaryIfAllDone(this.level, this.tracker);
        }

        public void testFailed(@NotNull GameTestInfo pTestInfo) {
            showTestSummaryIfAllDone(this.level, this.tracker);
        }
    }

    static void showTestSummaryIfAllDone(ServerLevel pServerLevel, MultipleTestTracker pTracker) {
        if (pTracker.isDone()) {
            say(pServerLevel, "GameTest done! " + pTracker.getTotalCount() + " tests were run", ChatFormatting.WHITE);
            if (pTracker.hasFailedRequired()) {
                say(pServerLevel, pTracker.getFailedRequiredCount() + " required tests failed :(", ChatFormatting.RED);
            } else {
                say(pServerLevel, "All required tests passed :)", ChatFormatting.GREEN);
            }

            if (pTracker.hasFailedOptional()) {
                say(pServerLevel, pTracker.getFailedOptionalCount() + " optional tests failed", ChatFormatting.GRAY);
            }
        }
    }

    private static void say(ServerLevel pServerLevel, String pMessage, ChatFormatting pFormatting) {
        pServerLevel.getPlayers((p_127945_) -> {
            return true;
        }).forEach((p_127990_) -> {
            p_127990_.sendSystemMessage(Component.literal(pFormatting + pMessage));
        });
    }
}
