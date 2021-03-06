package com.ldtteam.perviaminvenire.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ConfigurationManager {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final ConfigurationManager INSTANCE = new ConfigurationManager();

    public static ConfigurationManager getInstance() {
        return INSTANCE;
    }

    private CommonConfig commonConfig = null;

    private ConfigurationManager() {
    }

    public CommonConfig getCommonConfig() {
        return Validate.notNull(commonConfig, "Tried to access common configuration before initialization.");
    }

    public void ensureInitialized(final ModContainer modContainer) {
        LOGGER.info("PVI Configuration created.");
        final Pair<CommonConfig, ForgeConfigSpec> com = new ForgeConfigSpec.Builder().configure(CommonConfig::new);

        /**
         * Loaded on both sides, not synced. Values might differ.
         */
        final ModConfig common = new ModConfig(ModConfig.Type.COMMON, com.getRight(), modContainer);
        commonConfig = com.getLeft();

        modContainer.addConfig(common);
    }
}
