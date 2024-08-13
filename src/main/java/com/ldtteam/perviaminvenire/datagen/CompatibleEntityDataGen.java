package com.ldtteam.perviaminvenire.datagen;

import com.ldtteam.perviaminvenire.api.util.ModTags;
import com.ldtteam.perviaminvenire.api.util.constants.ModConstants;
import com.ldtteam.perviaminvenire.compat.vanilla.VanillaCompatibilityManager;
import com.ldtteam.perviaminvenire.util.EntityTypeUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = ModConstants.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class CompatibleEntityDataGen extends EntityTypeTagsProvider
{
    private static final Logger LOGGER           = LogManager.getLogger();

    @SubscribeEvent
    public static void onGatherData(final GatherDataEvent event)
    {
        LOGGER.info("Starting PVI Compatibility entity datagen.");
        VanillaCompatibilityManager.getInstance().initialize();

        event.getGenerator().addProvider(true, new CompatibleEntityDataGen(
          event.getGenerator().getPackOutput(),
          event.getLookupProvider(),
          event.getExistingFileHelper()
        ));
    }

    public CompatibleEntityDataGen(
      final PackOutput dataGenerator,
      final CompletableFuture<HolderLookup.Provider> lookupProvider,
      @Nullable final ExistingFileHelper existingFileHelper)
    {
        super(dataGenerator, lookupProvider, ModConstants.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        tag(ModTags.REPLACE_VANILLA_NAVIGATOR).add(EntityTypeUtils.getCompatibleVanillaOverrideTypes(provider));
    }

    @Override
    public @NotNull String getName()
    {
        return "Compatible PVI Navigator " + super.getName();
    }
}
