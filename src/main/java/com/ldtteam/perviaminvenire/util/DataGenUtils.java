package com.ldtteam.perviaminvenire.util;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.profiler.EmptyProfiler;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
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
        final DynamicRegistries.Impl dynamicRegistries = new DynamicRegistries.Impl();
        DimensionType.registerTypes(dynamicRegistries);
        final DimensionType overworldDimension = dynamicRegistries.getRegistry(Registry.DIMENSION_TYPE_KEY).getOrDefault(DimensionType.OVERWORLD_ID);

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
            public DynamicRegistries func_241828_r()
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
                         return mob.getNavigator().getClass() == GroundPathNavigator.class;
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
