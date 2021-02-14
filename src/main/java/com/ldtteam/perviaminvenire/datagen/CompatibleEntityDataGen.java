package com.ldtteam.perviaminvenire.datagen;

import com.ldtteam.perviaminvenire.api.pathfinding.registry.IPathNavigatorRegistry;
import com.ldtteam.perviaminvenire.api.util.ModTags;
import com.ldtteam.perviaminvenire.api.util.constants.ModConstants;
import com.ldtteam.perviaminvenire.compat.vanilla.VanillaCompatibilityManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.EntityTypeTagsProvider;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@Mod.EventBusSubscriber(modid = ModConstants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class CompatibleEntityDataGen extends EntityTypeTagsProvider
{
    @SubscribeEvent
    public static void onGatherData(final GatherDataEvent event)
    {
        LOGGER.info("Starting PVI Compatibility entity datagen.");
        VanillaCompatibilityManager.getInstance().initialize();

        event.getGenerator().addProvider(new CompatibleEntityDataGen(
          event.getGenerator(),
          event.getExistingFileHelper()
        ));
    }

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String MINECRAFT_MOD_ID = "minecraft";

    public CompatibleEntityDataGen(
      final DataGenerator dataGenerator,
      @Nullable final ExistingFileHelper existingFileHelper)
    {
        super(dataGenerator, ModConstants.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerTags()
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

        getOrCreateBuilder(ModTags.REPLACE_VANILLA_NAVIGATOR).add(ForgeRegistries.ENTITIES.getValues()
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
                                                                    }).toArray(EntityType<?>[]::new));
    }

    @Override
    public String getName()
    {
        return "Compatible PVI Navigator " + super.getName();
    }
}
