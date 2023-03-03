package com.ldtteam.perviaminvenire.util;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.client.ColorResolverManager;
import net.minecraftforge.client.DimensionSpecialEffectsManager;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

public class EntityTypeUtils {
    private static final Field movementControllerField = ObfuscationReflectionHelper.findField(
            Mob.class, "f_21342_"
    );

    private static final String MINECRAFT_MOD_ID = "minecraft";
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ClientLevel CLIENT_LEVEL = createInstantiationWorld();
    private static final Set<EntityType<?>> VANILLA_BOSSES = Set.of(EntityType.ENDER_DRAGON, EntityType.WITHER);
    private static final Set<EntityType<?>> NOT_MOVING = Set.of(EntityType.SHULKER);
    private static final Set<EntityType<?>> NOT_SUPPORTED = Set.of(EntityType.GIANT);
    private EntityTypeUtils() {
        throw new IllegalStateException("Can not instantiate an instance of: DataGenUtils. This is a utility class");
    }

    public static EntityType<?>[] getCompatibleVanillaOverrideTypes() {
        return getCompatibleVanillaOverrideTypes(mob -> {
            return isMobEntityASupportedGroundEntity(mob) ||
                    isMobEntityASupportedSwimmingEntity(mob) ||
                    isMobEntityASupportedClimberEntity(mob) ||
                    isMobEntityASupportedFlyingEntity(mob);
        });
    }

    public static EntityType<?>[] getCompatibleVanillaOverrideTypes(final Predicate<Mob> filter) {
        return ForgeRegistries.ENTITY_TYPES.getValues()
                .stream()
                .filter(entityType -> !NOT_SUPPORTED.contains(entityType))
                .filter(entityType -> !VANILLA_BOSSES.contains(entityType))
                .filter(entityType -> !NOT_MOVING.contains(entityType))
                .filter(entityType -> MINECRAFT_MOD_ID.equals(Objects.requireNonNull(ForgeRegistries.ENTITY_TYPES.getKey(entityType)).getNamespace()))
                .filter(entityType -> {
                    try {
                        final Entity entity = createEntityType(entityType);
                        if (!(entity instanceof final Mob mob))
                            return false;

                        return filter.test(mob);
                    } catch (Exception ex) {
                        LOGGER.error(String.format(
                                "Failed to create and validate entity, of type: '%s' for navigator replacement. Skipping!",
                                ForgeRegistries.ENTITY_TYPES.getKey(entityType)), ex);
                        return false;
                    }
                }).toArray(EntityType<?>[]::new);
    }


    @SuppressWarnings("UnstableApiUsage")
    @NotNull
    private static ClientLevel createInstantiationWorld() {
        final RegistryAccess.Writable dynamicRegistries = RegistryAccess.builtinCopy();
        final Holder<DimensionType> overworldDimension = dynamicRegistries.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY)
                .getOrCreateHolder(BuiltinDimensionTypes.OVERWORLD).getOrThrow(false, message -> {
                    throw new IllegalStateException("Failed to get overworld during datagen " + message);
                });

        ColorResolverManager.init();
        DimensionSpecialEffectsManager.init();

        @SuppressWarnings("ConstantConditions") //We are creating a dummy world here.
        final ClientLevel clientWorld = new ClientLevel(
                null,
                new ClientLevel.ClientLevelData(Difficulty.HARD, false, true),
                Level.OVERWORLD,
                overworldDimension,
                1,
                0,
                () -> InactiveProfiler.INSTANCE,
                null,
                true,
                0
        ) {
            @Override
            public @NotNull RegistryAccess registryAccess() {
                return dynamicRegistries;
            }
        };
        return clientWorld;
    }

    @Nullable
    public static <V extends Entity> V createEntityType(EntityType<V> entityType) {
        return entityType.create(EntityTypeUtils.CLIENT_LEVEL);
    }

    private static Class<?> getMovementControllerClass(final Mob mobEntity) {
        try {
            return movementControllerField.get(mobEntity).getClass();
        } catch (IllegalAccessException e) {
            return Object.class;
        }
    }

    public static boolean isMobEntityASupportedGroundEntity(final Mob mobEntity) {
        //We only support none overriden movement and ground path navigators.
        return mobEntity.getNavigation().getClass() == GroundPathNavigation.class &&
                getMovementControllerClass(mobEntity) == MoveControl.class
                && !(mobEntity instanceof WaterAnimal);

    }

    public static boolean isMobEntityASupportedSwimmingEntity(final Mob mobEntity) {
        //We only support none overriden movement and ground path navigators.
        return mobEntity.getNavigation().getClass() == GroundPathNavigation.class &&
                getMovementControllerClass(mobEntity) == MoveControl.class &&
                mobEntity instanceof WaterAnimal;

    }

    public static boolean isMobEntityASupportedClimberEntity(final Mob mobEntity) {
        //We only support none overriden movement and ground path navigators.
        return mobEntity.getNavigation().getClass() == WallClimberNavigation.class &&
                getMovementControllerClass(mobEntity) == MoveControl.class;

    }

    public static boolean isMobEntityASupportedFlyingEntity(final Mob mobEntity) {
        //We only support none overriden movement and ground path navigators.
        return mobEntity.getNavigation().getClass() == FlyingPathNavigation.class &&
                getMovementControllerClass(mobEntity) == FlyingMoveControl.class;

    }
}
