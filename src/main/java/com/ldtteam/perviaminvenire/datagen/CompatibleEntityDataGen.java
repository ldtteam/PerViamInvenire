package com.ldtteam.perviaminvenire.datagen;

import com.ldtteam.perviaminvenire.api.util.ModTags;
import com.ldtteam.perviaminvenire.api.util.constants.ModConstants;
import com.ldtteam.perviaminvenire.compat.vanilla.VanillaCompatibilityManager;
import com.ldtteam.perviaminvenire.util.DataGenUtils;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@Mod.EventBusSubscriber(modid = ModConstants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class CompatibleEntityDataGen extends EntityTypeTagsProvider
{
    private static final Logger LOGGER           = LogManager.getLogger();

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

    public CompatibleEntityDataGen(
      final DataGenerator dataGenerator,
      @Nullable final ExistingFileHelper existingFileHelper)
    {
        super(dataGenerator, ModConstants.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags()
    {
        tag(ModTags.REPLACE_VANILLA_NAVIGATOR).add(DataGenUtils.getCompatibleVanillaOverrideTypes());
    }

    @Override
    public String getName()
    {
        return "Compatible PVI Navigator " + super.getName();
    }
}
