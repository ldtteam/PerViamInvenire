package com.ldtteam.perviaminvenire.config;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ConfigTracker;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
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
        final Pair<CommonConfig, ModConfigSpec> com = new ModConfigSpec.Builder().configure(CommonConfig::new);
        modContainer.registerConfig(ModConfig.Type.COMMON, com.getRight());
        commonConfig = com.getLeft();
    }
}
