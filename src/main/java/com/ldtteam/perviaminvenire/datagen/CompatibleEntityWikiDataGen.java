package com.ldtteam.perviaminvenire.datagen;

import com.google.common.collect.Lists;
import com.google.common.hash.HashCode;
import com.ldtteam.perviaminvenire.api.util.constants.ModConstants;
import com.ldtteam.perviaminvenire.compat.vanilla.VanillaCompatibilityManager;
import com.ldtteam.perviaminvenire.util.DataGenUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.HashCache;
import net.minecraft.data.DataProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.locale.Language;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.function.BinaryOperator;

@Mod.EventBusSubscriber(modid = ModConstants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class CompatibleEntityWikiDataGen implements DataProvider
{
    private static final Logger LOGGER           = LogManager.getLogger();

    @SubscribeEvent
    public static void onGatherData(final GatherDataEvent event)
    {
        LOGGER.info("Starting PVI Compatibility entity wiki datagen.");
        VanillaCompatibilityManager.getInstance().initialize();

        event.getGenerator().addProvider(new CompatibleEntityWikiDataGen(
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
    public void run(final HashCache cache) throws IOException
    {
        final EntityType<?>[] types = DataGenUtils.getCompatibleVanillaOverrideTypes();
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

        Files.write(path, lines, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        final HashCode hash = SHA1.hashUnencodedChars(
          lines.stream().reduce("", (s, s2) -> String.format("%s\n%s", s, s2))
        );

        cache.putNew(path, hash.toString());

    }

    @Override
    public String getName()
    {
        return "Compatible PVI Navigator Wiki Entries Generator.";
    }
}
