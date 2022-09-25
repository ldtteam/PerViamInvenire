package com.ldtteam.perviaminvenire.test;

import com.ldtteam.perviaminvenire.api.adapters.registry.IBoundingBoxProducerRegistry;
import com.ldtteam.perviaminvenire.api.util.constants.ModConstants;
import com.ldtteam.perviaminvenire.test.execution.SimpleWalkTestExecutor;
import com.ldtteam.perviaminvenire.test.level.EmptyLevelReader;
import com.ldtteam.perviaminvenire.test.template.TemplatePackManager;
import com.ldtteam.perviaminvenire.util.EntityTypeUtils;
import com.mojang.logging.LogUtils;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Mod.EventBusSubscriber(modid = ModConstants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PVITestFramework {

    private static final Logger LOGGER = LogUtils.getLogger();

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
/*        final EntityType<?>[] supportedGroundEntities = EntityTypeUtils.getCompatibleVanillaOverrideTypes(EntityTypeUtils::isMobEntityASupportedGroundEntity);
        for (EntityType<?> supportedEntityType : supportedGroundEntities) {
            LOGGER.debug("Creating simple walking test for: %s".formatted(Objects.requireNonNull(ForgeRegistries.ENTITY_TYPES.getKey(supportedEntityType)).toString()));
            tests.addAll(buildSimpleWalkingTestsFunctionFor(supportedEntityType, "Ground"));
        }

        final EntityType<?>[] supportedCliming = EntityTypeUtils.getCompatibleVanillaOverrideTypes(EntityTypeUtils::isMobEntityASupportedClimberEntity);
        for (EntityType<?> supportedEntityType : supportedCliming) {
            LOGGER.debug("Creating simple walking test for: %s".formatted(Objects.requireNonNull(ForgeRegistries.ENTITY_TYPES.getKey(supportedEntityType)).toString()));
            tests.addAll(buildSimpleWalkingTestsFunctionFor(supportedEntityType, "Climber"));
        }*/

        tests.addAll(buildSimpleWalkingTestsFunctionFor(EntityType.SQUID, "Squid"));
        return tests;
    }

    @SuppressWarnings("unchecked")
    @NotNull
    private static <V extends Entity> Collection<TestFunction> buildSimpleWalkingTestsFunctionFor(final EntityType<V> supportedEntityType, final String batchPrefix) {
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
                .getRunner().produce(entity, new Vec3(0,0,0), new Vec3(1,0,0), EmptyLevelReader.INSTANCE)
                .orElseGet(() -> {
                    final EntityDimensions entitySize = entity.getDimensions(entity.getPose());
                    return AABB.ofSize(Vec3.ZERO, entitySize.width, entitySize.height, entitySize.width);
                });

        final ResourceLocation testName = new ResourceLocation(ModConstants.MOD_ID, "simple_%s_%s".formatted(entityName.getNamespace(), entityName.getPath()));
        Block fillBlock = Blocks.AIR;
        if (entity instanceof WaterAnimal) {
            fillBlock = Blocks.WATER;
        }
        TemplatePackManager.getInstance().createSimpleWalkTemplateFor(testName, Blocks.STONE, fillBlock, determineWidth(entityBox), determineHeight(entityBox));

        for (Rotation rotation : Rotation.values()) {
            final String directionName = rotation.rotate(Direction.EAST).getSerializedName().toLowerCase();

            final TestFunction test = new TestFunction(
                    "%s simple walking %s".formatted(batchPrefix, directionName),
                    entityName.toString(),
                    testName.toString(),
                    rotation,
                    400,
                    20,
                    true,
                    SimpleWalkTestExecutor.getInstance().createSimpleWalkTestExecutionFor(mobEntityType, determineWidth(entityBox))
            );

            testsForEntityType.add(test);
        }

        return testsForEntityType;
    }

    private static int determineWidth(final AABB box) {
        return (int) Math.ceil(Math.max(box.getXsize(), box.getZsize()));
    }

    private static int determineHeight(final AABB box) {
        return (int) Math.ceil(box.getYsize());
    }
}
