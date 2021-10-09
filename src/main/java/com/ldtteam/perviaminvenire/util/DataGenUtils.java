package com.ldtteam.perviaminvenire.util;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.controller.FlyingMovementController;
import net.minecraft.entity.ai.controller.MovementController;
import net.minecraft.pathfinding.ClimberPathNavigator;
import net.minecraft.pathfinding.FlyingPathNavigator;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.profiler.EmptyProfiler;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.Objects;

public class DataGenUtils
{
    private static       Field  movementControllerField = ObfuscationReflectionHelper.findField(
      MobEntity.class, "field_70765_h"
    );

    private static final String MINECRAFT_MOD_ID        = "minecraft";
    private static final Logger LOGGER           = LogManager.getLogger();

    private DataGenUtils()
    {
        throw new IllegalStateException("Can not instantiate an instance of: DataGenUtils. This is a utility class");
    }

    public static EntityType<?>[] getCompatibleVanillaOverrideTypes()
    {
        final DynamicRegistries.Impl dynamicRegistries = new DynamicRegistries.Impl();
        DimensionType.registerBuiltin(dynamicRegistries);
        final DimensionType overworldDimension = dynamicRegistries.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY).get(DimensionType.OVERWORLD_EFFECTS);

        @SuppressWarnings("ConstantConditions") //We are creating a dummy world here.
        final ClientWorld clientWorld = new ClientWorld(
          null,
          new ClientWorld.ClientWorldInfo(Difficulty.HARD, false, true),
          World.OVERWORLD,
          overworldDimension,
          1,
          () -> EmptyProfiler.INSTANCE,
          null,
          true,
          0
        ) {
            @Override
            public DynamicRegistries registryAccess()
            {
                return dynamicRegistries;
            }
        };

        return ForgeRegistries.ENTITIES.getValues()
                 .stream()
                 .filter(entityType -> MINECRAFT_MOD_ID.equals(Objects.requireNonNull(entityType.getRegistryName())
                                                                 .getNamespace()))
                 .filter(entityType -> {
                     try
                     {
                         final Entity entity = entityType.create(clientWorld);
                         if (!(entity instanceof MobEntity))
                             return false;

                         final MobEntity mob = (MobEntity) entity;
                         return isMobEntityASupportedGroundEntity(mob) ||
                                  isMobEntityASupportedClimberEntity(mob) ||
                           isMobEntityASupportedFlyingEntity(mob);
                     }
                     catch (Exception ex)
                     {
                         LOGGER.error(String.format(
                           "Failed to create and validate entity, of type: '%s' for navigator replacement. Skipping!",
                           entityType.getRegistryName()), ex);
                         return false;
                     }
                 }).toArray(EntityType<?>[]::new);
    }

    private static Class<?> getMovementControllerClass(final MobEntity mobEntity) {
        try
        {
            return movementControllerField.get(mobEntity).getClass();
        }
        catch (IllegalAccessException e)
        {
            return Object.class;
        }
    }

    private static boolean isMobEntityASupportedGroundEntity(final MobEntity mobEntity) {
        //We only support none overriden movement and ground path navigators.
        return mobEntity.getNavigation().getClass() == GroundPathNavigator.class &&
                 getMovementControllerClass(mobEntity) == MovementController.class;

    }

    private static boolean isMobEntityASupportedClimberEntity(final MobEntity mobEntity) {
        //We only support none overriden movement and ground path navigators.
        return mobEntity.getNavigation().getClass() == ClimberPathNavigator.class &&
                 getMovementControllerClass(mobEntity) == MovementController.class;

    }

    private static boolean isMobEntityASupportedFlyingEntity(final MobEntity mobEntity) {
        //We only support none overriden movement and ground path navigators.
        return mobEntity.getNavigation().getClass() == FlyingPathNavigator.class &&
                 getMovementControllerClass(mobEntity) == FlyingMovementController.class;

    }
}
