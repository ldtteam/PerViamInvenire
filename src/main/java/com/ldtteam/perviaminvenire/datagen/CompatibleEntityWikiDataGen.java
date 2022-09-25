package com.ldtteam.perviaminvenire.datagen;

import com.google.common.collect.Lists;
import com.google.common.hash.HashCode;
import com.ldtteam.perviaminvenire.api.util.constants.ModConstants;
import com.ldtteam.perviaminvenire.compat.vanilla.VanillaCompatibilityManager;
import com.ldtteam.perviaminvenire.util.EntityTypeUtils;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.locale.Language;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Mod.EventBusSubscriber(modid = ModConstants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class CompatibleEntityWikiDataGen implements DataProvider
{
    private static final Logger LOGGER           = LogManager.getLogger();

    @SubscribeEvent
    public static void onGatherData(final GatherDataEvent event)
    {
        LOGGER.info("Starting PVI Compatibility entity wiki datagen.");
        VanillaCompatibilityManager.getInstance().initialize();

        event.getGenerator().addProvider(true, new CompatibleEntityWikiDataGen(
          event.getGenerator(),
          event.getExistingFileHelper()
        ));
    }

    private final DataGenerator generator;
    private final ExistingFileHelper existingFileHelper;

    public CompatibleEntityWikiDataGen(
      @NotNull final DataGenerator generator,
      @Nullable final ExistingFileHelper existingFileHelper)
    {
        this.generator = generator;
        this.existingFileHelper = existingFileHelper;
    }

    @Override
    public void run(final @NotNull CachedOutput cachedOutput) throws IOException
    {
        final EntityType<?>[] types = EntityTypeUtils.getCompatibleVanillaOverrideTypes();
        final Path path = this.generator.getOutputFolder().resolve("wiki/" + ModConstants.MOD_ID + "/tags/entity_types/replace_vanilla_navigator.md");

        final List<String> lines = Lists.newArrayList();
        lines.add("#### Compatible Entities:");
        lines.add("");
        for (final EntityType<?> type : types)
        {
            lines.add(String.format("- %s", Language.getInstance().getOrDefault(type.getDescriptionId())));
        }

        if (path.getParent().toFile().mkdirs())
            LOGGER.info(String.format("Created directory for: %s", path.getParent()));

        cachedOutput.writeIfNeeded(path, String.join("\n", lines).getBytes(), HashCode.fromString(String.join("\n", lines)));
    }

    @Override
    public @NotNull String getName()
    {
        return "Compatible PVI Navigator Wiki Entries Generator.";
    }
}
