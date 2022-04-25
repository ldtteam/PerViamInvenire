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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Objects;

public class DataGenUtils
{
    private static final Field movementControllerField = ObfuscationReflectionHelper.findField(
      Mob.class, "f_21342_"
    );

    private static final String MINECRAFT_MOD_ID        = "minecraft";
    private static final Logger LOGGER           = LogManager.getLogger();

    private DataGenUtils()
    {
        throw new IllegalStateException("Can not instantiate an instance of: DataGenUtils. This is a utility class");
    }

    public static EntityType<?>[] getCompatibleVanillaOverrideTypes()
    {
        final RegistryAccess.Writable dynamicRegistries = RegistryAccess.builtinCopy();
        final Holder<DimensionType> overworldDimension = dynamicRegistries.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY)
              .getOrCreateHolder(DimensionType.OVERWORLD_LOCATION);

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
            public @NotNull RegistryAccess registryAccess()
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
                         if (!(entity instanceof final Mob mob))
                             return false;

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

    private static Class<?> getMovementControllerClass(final Mob mobEntity) {
        try
        {
            return movementControllerField.get(mobEntity).getClass();
        }
        catch (IllegalAccessException e)
        {
            return Object.class;
        }
    }

    private static boolean isMobEntityASupportedGroundEntity(final Mob mobEntity) {
        //We only support none overriden movement and ground path navigators.
        return mobEntity.getNavigation().getClass() == GroundPathNavigation.class &&
                 getMovementControllerClass(mobEntity) == MoveControl.class;

    }

    private static boolean isMobEntityASupportedClimberEntity(final Mob mobEntity) {
        //We only support none overriden movement and ground path navigators.
        return mobEntity.getNavigation().getClass() == WallClimberNavigation.class &&
                 getMovementControllerClass(mobEntity) == MoveControl.class;

    }

    private static boolean isMobEntityASupportedFlyingEntity(final Mob mobEntity) {
        //We only support none overriden movement and ground path navigators.
        return mobEntity.getNavigation().getClass() == FlyingPathNavigation.class &&
                 getMovementControllerClass(mobEntity) == FlyingMoveControl.class;

    }
}
