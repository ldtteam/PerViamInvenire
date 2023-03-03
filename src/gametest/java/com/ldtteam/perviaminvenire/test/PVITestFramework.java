package com.ldtteam.perviaminvenire.test;

import com.google.common.collect.ImmutableMap;
import com.ldtteam.perviaminvenire.api.adapters.registry.IBoundingBoxProducerRegistry;
import com.ldtteam.perviaminvenire.api.util.constants.ModConstants;
import com.ldtteam.perviaminvenire.test.execution.WalkTestExecutor;
import com.ldtteam.perviaminvenire.test.level.EmptyLevelReader;
import com.ldtteam.perviaminvenire.test.template.TemplatePackManager;
import com.ldtteam.perviaminvenire.util.EntityTypeUtils;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("ConstantConditions")
@Mod.EventBusSubscriber(modid = ModConstants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PVITestFramework {

    private static final Set<Block> NONE_PASSABLE_BLOCKS = Set.of(
            Blocks.BAMBOO,
            Blocks.CACTUS,
            Blocks.AZALEA,
            Blocks.FLOWERING_AZALEA
    );

    private static final int PASSABLE_BLOCK_COUNT = 2;

    private static final int MAX_JUMP_STEP_COUNT = 2;

    private static final Logger LOGGER = LogUtils.getLogger();

    private record WalkThroughBlockInformation(BlockState baseState, BlockState noneBaseState, BlockState soil) {}

    @SubscribeEvent
    public static void onRegisterGameTests(RegisterGameTestsEvent event) {
        event.register(PVITestFramework.class);
    }

    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent event) {
        event.addRepositorySource(TemplatePackManager.getInstance());
    }

    @SuppressWarnings("unused")
    @GameTestGenerator
    public static Collection<TestFunction> generateSimpleWalkingTests() {
        LOGGER.info("Creating simple walking tests.");

        final List<TestFunction> tests = new ArrayList<>();
        final EntityType<?>[] supportedGroundEntities = EntityTypeUtils.getCompatibleVanillaOverrideTypes(EntityTypeUtils::isMobEntityASupportedGroundEntity);
        for (EntityType<?> supportedEntityType : supportedGroundEntities) {
            LOGGER.debug("Creating simple walking test for: %s".formatted(Objects.requireNonNull(ForgeRegistries.ENTITY_TYPES.getKey(supportedEntityType)).toString()));
            tests.addAll(buildSimpleWalkingTestsFunctionFor(supportedEntityType, "Ground", false));
        }

        final EntityType<?>[] supportedSwimmingEntities = EntityTypeUtils.getCompatibleVanillaOverrideTypes(EntityTypeUtils::isMobEntityASupportedSwimmingEntity);
        for (EntityType<?> supportedEntityType : supportedSwimmingEntities) {
            LOGGER.debug("Creating simple walking test for: %s".formatted(Objects.requireNonNull(ForgeRegistries.ENTITY_TYPES.getKey(supportedEntityType)).toString()));
            tests.addAll(buildSimpleWalkingTestsFunctionFor(supportedEntityType, "Swimming", false));
        }

        final EntityType<?>[] supportedCliming = EntityTypeUtils.getCompatibleVanillaOverrideTypes(EntityTypeUtils::isMobEntityASupportedClimberEntity);
        for (EntityType<?> supportedEntityType : supportedCliming) {
            LOGGER.debug("Creating simple walking test for: %s".formatted(Objects.requireNonNull(ForgeRegistries.ENTITY_TYPES.getKey(supportedEntityType)).toString()));
            tests.addAll(buildSimpleWalkingTestsFunctionFor(supportedEntityType, "Climber", false));
        }

        LOGGER.warn("Registered %s simple walking tests.".formatted(tests.size()));

        return tests;
    }

    @SuppressWarnings("unused")
    @GameTestGenerator
    public static Collection<TestFunction> generateWalkThroughWalkingTests() {
        LOGGER.info("Creating walk through walking tests.");

        final List<TestFunction> tests = new ArrayList<>();
        final List<WalkThroughBlockInformation> sourceGroundWalkthroughBlocks = new ArrayList<>();
        sourceGroundWalkthroughBlocks.add(new WalkThroughBlockInformation(Blocks.GRASS.defaultBlockState(), Blocks.AIR.defaultBlockState(), Blocks.DIRT.defaultBlockState()));

        ForgeRegistries.BLOCKS.getValues().stream()
                .filter(FlowerPotBlock.class::isInstance)
                .map(FlowerPotBlock.class::cast)
                .map(FlowerPotBlock::getContent)
                .filter(block -> !(block instanceof LiquidBlockContainer))
                .filter(block -> !NONE_PASSABLE_BLOCKS.contains(block))
                .forEach(flower -> sourceGroundWalkthroughBlocks.add(new WalkThroughBlockInformation(flower.defaultBlockState(), Blocks.AIR.defaultBlockState(), Blocks.DIRT.defaultBlockState())));
        ForgeRegistries.BLOCKS.getValues().stream()
                        .filter(DoublePlantBlock.class::isInstance)
                        .map(DoublePlantBlock.class::cast)
                        .filter(doublePlant -> !(doublePlant instanceof LiquidBlockContainer)) //Exclude water plants
                        .filter(block -> !NONE_PASSABLE_BLOCKS.contains(block))
                        .forEach(doublePlant -> {
                            sourceGroundWalkthroughBlocks.add(new WalkThroughBlockInformation(doublePlant.defaultBlockState(), doublePlant.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER), Blocks.DIRT.defaultBlockState()));
                        });

        Collections.shuffle(sourceGroundWalkthroughBlocks);
        final List<WalkThroughBlockInformation> groundWalkthroughBlocks = sourceGroundWalkthroughBlocks.subList(0, Math.min(sourceGroundWalkthroughBlocks.size(), PASSABLE_BLOCK_COUNT));

        final EntityType<?>[] supportedGroundEntities = EntityTypeUtils.getCompatibleVanillaOverrideTypes(EntityTypeUtils::isMobEntityASupportedGroundEntity);
        groundWalkthroughBlocks.forEach(walkThroughBlockInformation -> {
            for (EntityType<?> supportedEntityType : supportedGroundEntities) {
                LOGGER.debug("Creating walk through walking test for: %s through %s and %s on %s".formatted(
                        ForgeRegistries.ENTITY_TYPES.getKey(supportedEntityType),
                        ForgeRegistries.BLOCKS.getKey(walkThroughBlockInformation.baseState().getBlock()),
                        ForgeRegistries.BLOCKS.getKey(walkThroughBlockInformation.noneBaseState.getBlock()),
                        ForgeRegistries.BLOCKS.getKey(walkThroughBlockInformation.soil.getBlock())));

                tests.addAll(buildWalkThroughWalkingTestsFunctionFor(supportedEntityType, "Ground", walkThroughBlockInformation.baseState(), walkThroughBlockInformation.noneBaseState(), walkThroughBlockInformation.soil(), false));
            }
        });

        final List<WalkThroughBlockInformation> sourceSwimmerWalkthroughBlocks = new ArrayList<>();
        sourceSwimmerWalkthroughBlocks.add(new WalkThroughBlockInformation(Blocks.SEAGRASS.defaultBlockState(), Blocks.WATER.defaultBlockState(), Blocks.STONE.defaultBlockState()));
        sourceSwimmerWalkthroughBlocks.add(new WalkThroughBlockInformation(Blocks.TALL_SEAGRASS.defaultBlockState(), Blocks.TALL_SEAGRASS.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER), Blocks.STONE.defaultBlockState()));
        sourceSwimmerWalkthroughBlocks.add(new WalkThroughBlockInformation(Blocks.KELP.defaultBlockState(), Blocks.WATER.defaultBlockState(), Blocks.STONE.defaultBlockState()));
        sourceSwimmerWalkthroughBlocks.add(new WalkThroughBlockInformation(Blocks.KELP_PLANT.defaultBlockState(), Blocks.KELP.defaultBlockState(), Blocks.STONE.defaultBlockState()));

        Collections.shuffle(sourceSwimmerWalkthroughBlocks);
        final List<WalkThroughBlockInformation> swimmerWalkthroughBlocks = sourceSwimmerWalkthroughBlocks.subList(0, Math.min(sourceSwimmerWalkthroughBlocks.size(), PASSABLE_BLOCK_COUNT));

        final EntityType<?>[] supportedSwimmingEntities = EntityTypeUtils.getCompatibleVanillaOverrideTypes(EntityTypeUtils::isMobEntityASupportedSwimmingEntity);
        swimmerWalkthroughBlocks.forEach(walkThroughBlockInformation -> {
            for (EntityType<?> supportedEntityType : supportedSwimmingEntities) {
                LOGGER.debug("Creating swim through swimming test for: %s through %s and %s on %s".formatted(
                        ForgeRegistries.ENTITY_TYPES.getKey(supportedEntityType),
                        ForgeRegistries.BLOCKS.getKey(walkThroughBlockInformation.baseState().getBlock()),
                        ForgeRegistries.BLOCKS.getKey(walkThroughBlockInformation.noneBaseState.getBlock()),
                        ForgeRegistries.BLOCKS.getKey(walkThroughBlockInformation.soil.getBlock())));

                tests.addAll(buildWalkThroughWalkingTestsFunctionFor(supportedEntityType, "Swimming", walkThroughBlockInformation.baseState(), walkThroughBlockInformation.noneBaseState(), walkThroughBlockInformation.soil(), false));
            }
        });

        final EntityType<?>[] supportedClimbing = EntityTypeUtils.getCompatibleVanillaOverrideTypes(EntityTypeUtils::isMobEntityASupportedClimberEntity);
        groundWalkthroughBlocks.forEach(walkThroughBlockInformation -> {
            for (EntityType<?> supportedEntityType : supportedClimbing) {
                LOGGER.debug("Creating walkthrough walking test for: %s through %s and %s on %s".formatted(
                        ForgeRegistries.ENTITY_TYPES.getKey(supportedEntityType),
                        ForgeRegistries.BLOCKS.getKey(walkThroughBlockInformation.baseState().getBlock()),
                        ForgeRegistries.BLOCKS.getKey(walkThroughBlockInformation.noneBaseState.getBlock()),
                        ForgeRegistries.BLOCKS.getKey(walkThroughBlockInformation.soil.getBlock())));

                tests.addAll(buildWalkThroughWalkingTestsFunctionFor(supportedEntityType, "Climber", walkThroughBlockInformation.baseState(), walkThroughBlockInformation.noneBaseState(), walkThroughBlockInformation.soil(), false));
            }
        });

        LOGGER.warn("Registered %s walkthrough walking tests.".formatted(tests.size()));

        return tests;
    }

    @SuppressWarnings("unused")
    @GameTestGenerator
    public static Collection<TestFunction> generateJumpTests() {
        LOGGER.info("Creating jump tests.");

        final Direction[] directions = new Direction[] { Direction.UP, Direction.DOWN};

        final List<TestFunction> tests = new ArrayList<>();
        final EntityType<?>[] supportedGroundEntities = EntityTypeUtils.getCompatibleVanillaOverrideTypes(EntityTypeUtils::isMobEntityASupportedGroundEntity);
        final EntityType<?>[] supportedSwimmingEntities = EntityTypeUtils.getCompatibleVanillaOverrideTypes(EntityTypeUtils::isMobEntityASupportedSwimmingEntity);
        final EntityType<?>[] supportedCliming = EntityTypeUtils.getCompatibleVanillaOverrideTypes(EntityTypeUtils::isMobEntityASupportedClimberEntity);

        final Function<Direction, BlockState> noStairBuilder = direction -> Blocks.AIR.defaultBlockState();

        for (int stepCount = 1; stepCount <= MAX_JUMP_STEP_COUNT; stepCount++) {
            for (Direction direction : directions) {
                final Function<Direction, BlockState> stairBuilder = facing -> {
                    if (direction.getAxisDirection() == Direction.AxisDirection.NEGATIVE)
                        facing = facing.getOpposite();

                    return Blocks.STONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, facing);
                };

                for (EntityType<?> supportedEntityType : supportedGroundEntities) {
                    LOGGER.debug("Creating jump %s walking test for: %s with count %s".formatted(direction.getName().toLowerCase(), Objects.requireNonNull(ForgeRegistries.ENTITY_TYPES.getKey(supportedEntityType)).toString(), stepCount));
                    tests.addAll(buildJumpTestsFunctionFor(supportedEntityType, "Ground", direction, stepCount, noStairBuilder, false));
                    tests.addAll(buildJumpTestsFunctionFor(supportedEntityType, "Ground", direction, stepCount, stairBuilder, false));
                }

                for (EntityType<?> supportedEntityType : supportedSwimmingEntities) {
                    LOGGER.debug("Creating jump %s walking test for: %s with count %s".formatted(direction.getName().toLowerCase(), Objects.requireNonNull(ForgeRegistries.ENTITY_TYPES.getKey(supportedEntityType)).toString(), stepCount));
                    tests.addAll(buildJumpTestsFunctionFor(supportedEntityType, "Swimming", direction, stepCount, noStairBuilder, false));
                    tests.addAll(buildJumpTestsFunctionFor(supportedEntityType, "Swimming", direction, stepCount, stairBuilder, false));
                }

                for (EntityType<?> supportedEntityType : supportedCliming) {
                    LOGGER.debug("Creating jump %s walking test for: %s with count %s".formatted(direction.getName().toLowerCase(), Objects.requireNonNull(ForgeRegistries.ENTITY_TYPES.getKey(supportedEntityType)).toString(), stepCount));
                    tests.addAll(buildJumpTestsFunctionFor(supportedEntityType, "Climbing", direction, stepCount, noStairBuilder, false));
                    tests.addAll(buildJumpTestsFunctionFor(supportedEntityType, "Climbing", direction, stepCount, stairBuilder, false));
                }
            }
        }

        LOGGER.warn("Registered %s jump tests.".formatted(tests.size()));

        return tests;
    }

    @GameTestGenerator
    public static Collection<TestFunction> generateManualTests() {
        final List<TestFunction> tests = new ArrayList<>();

        tests.addAll(buildSimpleWalkingTestsFunctionFor(EntityType.SPIDER, "Spider", true));
        tests.addAll(buildSimpleWalkingTestsFunctionFor(EntityType.VILLAGER, "Villager", true));
        tests.addAll(buildSimpleWalkingTestsFunctionFor(EntityType.POLAR_BEAR, "PolarBear", true));

        return tests;
    }

    @NotNull
    private static <V extends Entity> Collection<TestFunction> buildSimpleWalkingTestsFunctionFor(final EntityType<V> supportedEntityType, final String batchPrefix, boolean manual) {
        return buildFunctionsFor(
                supportedEntityType,
                (entityName, facing) -> new ResourceLocation(ModConstants.MOD_ID, "simple_%s_%s".formatted(entityName.getNamespace(), entityName.getPath())),
                (testName, ground, filler, facing, width, height) -> TemplatePackManager.getInstance().createSimpleWalkTemplateFor(testName, ground, filler, width, height),
                rotationName -> "%s simple walking %s".formatted(batchPrefix, rotationName),
                (mobEntityType, width) -> WalkTestExecutor.getInstance().createSimpleWalkTestExecutionFor(mobEntityType, width),
                manual);
    }

    @NotNull
    private static <V extends Entity> Collection<TestFunction> buildWalkThroughWalkingTestsFunctionFor(final EntityType<V> supportedEntityType, final String batchPrefix, final BlockState groundLevelCollisionBlock, final BlockState noneGroundLevelCollisionBlock, final BlockState soilBlock, boolean manual) {
        final String groundCollisionBlockName = ForgeRegistries.BLOCKS.getKey(groundLevelCollisionBlock.getBlock()).toString().replace(":", "_");
        final String noneGroundCollisionBlockName = ForgeRegistries.BLOCKS.getKey(noneGroundLevelCollisionBlock.getBlock()).toString().replace(":", "_");
        final String soilBlockName = ForgeRegistries.BLOCKS.getKey(soilBlock.getBlock()).toString().replace(":", "_");
        return buildFunctionsFor(
                supportedEntityType,
                (entityName, facing) -> new ResourceLocation(ModConstants.MOD_ID, "walkthrough_%s_%s_through_%s_%s_on_%s".formatted(entityName.getNamespace(), entityName.getPath(), groundCollisionBlockName, noneGroundCollisionBlockName, soilBlockName)),
                (testName, ground, filler, facing, width, height) -> TemplatePackManager.getInstance().createWalkThroughTemplateFor(testName, ground, filler, groundLevelCollisionBlock, noneGroundLevelCollisionBlock, soilBlock, width, height),
                rotationName -> "%s walking %s through %s and %s on %s".formatted(batchPrefix, rotationName, groundCollisionBlockName, noneGroundCollisionBlockName, soilBlockName),
                (mobEntityType, width) -> WalkTestExecutor.getInstance().createSimpleWalkTestExecutionFor(mobEntityType, width),
                manual);
    }

    @NotNull
    private static <V extends Entity> Collection<TestFunction> buildJumpTestsFunctionFor(final EntityType<V> supportedEntityType, final String batchPrefix, final Direction direction, final int stepCount, final Function<Direction, BlockState> stairBuilder, boolean manual) {
        return buildFunctionsFor(
                supportedEntityType,
                (entityName, facing) -> new ResourceLocation(ModConstants.MOD_ID, "walk_%s_%s_%s_%s_times_with_%s".formatted(direction.getName().toLowerCase(), entityName.getNamespace(), entityName.getPath(), stepCount, ForgeRegistries.BLOCKS.getKey(stairBuilder.apply(facing).getBlock()).toString().replace(":", "_"))),
                (testName, ground, filler, rotation, width, height) -> TemplatePackManager.getInstance().createVerticalOffsetTemplateFor(testName, ground, filler, direction, width, stepCount, stairBuilder.apply(rotation) , width, height),
                facing -> "%s jumping %s rotated towards the %s for %s times with help of %s".formatted(batchPrefix, direction.getName().toLowerCase(), facing.getName().toLowerCase(), stepCount, ForgeRegistries.BLOCKS.getKey(stairBuilder.apply(facing).getBlock())),
                (mobEntityType, width) -> WalkTestExecutor.getInstance().createJumpWalkTestExecutionFor(mobEntityType, width, direction, stepCount),
                manual
        );
    }


    @SuppressWarnings("unchecked")
    @NotNull
    private static <V extends Entity> Collection<TestFunction> buildFunctionsFor(final EntityType<V> supportedEntityType, final ITestNameBuilder testNameBuilder, final ITestTemplateBuilder templateBuilder, final Function<Direction, String> testDisplayNameBuilder, final ITestExecutionBuilder testExecutionBuilder, boolean manual) {
        final Collection<TestFunction> testsForEntityType = new ArrayList<>();
        final ResourceLocation entityName = ForgeRegistries.ENTITY_TYPES.getKey(supportedEntityType);
        if (entityName == null)
            return List.of();
        final V entity = EntityTypeUtils.createEntityType(supportedEntityType);
        if (entity == null)
            return List.of();
        if (!(entity instanceof Mob))
            return List.of();

        final EntityType<? extends Mob> mobEntityType = (EntityType<? extends Mob>) supportedEntityType;

        final AABB entityBox = IBoundingBoxProducerRegistry.getInstance()
                .getRunner().produce(entity)
                .orElseGet(() -> {
                    final EntityDimensions entitySize = entity.getDimensions(entity.getPose());
                    return AABB.ofSize(Vec3.ZERO, entitySize.width, entitySize.height, entitySize.width);
                });

        for (Rotation rotation : Rotation.values()) {
            final Direction facing = rotation.rotate(Direction.EAST);
            final ResourceLocation testName = testNameBuilder.build(entityName, facing);
            Block fillBlock = Blocks.AIR;
            if (entity instanceof WaterAnimal) {
                fillBlock = Blocks.WATER;
            }
            templateBuilder.build(testName, Blocks.STONE, fillBlock, facing, determineWidth(entityBox), determineHeight(entityBox));

            final TestFunction test = new TestFunction(
                    (manual ? "[MANUAL]: " : "") + testDisplayNameBuilder.apply(facing),
                    entityName.toString(),
                    testName.toString(),
                    rotation,
                    400,
                    20,
                    true,
                    testExecutionBuilder.build(mobEntityType, determineWidth(entityBox))
            );

            testsForEntityType.add(test);
        }

        return testsForEntityType;
    }


    private static int determineWidth(final AABB box) {
        final int width = (int) Math.ceil(Math.max(box.getXsize(), box.getZsize()));
        if (width > 1) {
            return width + 1;
        }

        return width;
    }

    private static int determineHeight(final AABB box) {
        return (int) Math.ceil(box.getYsize());
    }

    private interface ITestNameBuilder {
        ResourceLocation build(ResourceLocation entityName, Direction testRotation);
    }

    private interface ITestTemplateBuilder {
        void build(final ResourceLocation testName, final Block ground, final Block filler, final Direction rotation, final int width, final int height);
    }

    private interface ITestExecutionBuilder {
        Consumer<GameTestHelper> build(final EntityType<? extends Mob> mobEntityType, final int width);
    }
}
