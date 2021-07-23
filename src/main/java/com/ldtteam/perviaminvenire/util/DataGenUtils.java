package com.ldtteam.perviaminvenire.util;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

public class DataGenUtils
{

    private static final String MINECRAFT_MOD_ID = "minecraft";
    private static final Logger LOGGER           = LogManager.getLogger();

    private DataGenUtils()
    {
        throw new IllegalStateException("Can not instantiate an instance of: DataGenUtils. This is a utility class");
    }

    public static EntityType<?>[] getCompatibleVanillaOverrideTypes()
    {
        final RegistryAccess.RegistryHolder dynamicRegistries = new RegistryAccess.RegistryHolder();
        DimensionType.registerBuiltin(dynamicRegistries);
        final DimensionType overworldDimension = dynamicRegistries.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY).get(DimensionType.OVERWORLD_EFFECTS);

        @SuppressWarnings("ConstantConditions") //We are creating a dummy world here.
        final ClientLevel clientWorld = new ClientLevel(
          null,
          new ClientLevel.ClientLevelData(Difficulty.HARD, false, true),
          Level.OVERWORLD,
          overworldDimension,
          1,
          () -> InactiveProfiler.INSTANCE,
          null,
          true,
          0
        ) {
            @Override
            public RegistryAccess registryAccess()
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
                         if (!(entity instanceof Mob))
                             return false;

                         final Mob mob = (Mob) entity;
                         return mob.getNavigation().getClass() == GroundPathNavigation.class ||
                            mob.getNavigation().getClass() == WallClimberNavigation.class;
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
}
