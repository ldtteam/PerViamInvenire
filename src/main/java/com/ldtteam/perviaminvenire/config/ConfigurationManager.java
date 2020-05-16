package com.ldtteam.perviaminvenire.config;

import com.mojang.brigadier.Command;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

public final class ConfigurationManager {

    private static final ConfigurationManager INSTANCE = new ConfigurationManager();

    public static ConfigurationManager getInstance() {
        return INSTANCE;
    }

    private final CommonConfig commonConfig;

    private ConfigurationManager() {
        final Pair<CommonConfig, ForgeConfigSpec> commonConfigForgeConfigSpecPair = new ForgeConfigSpec.Builder().configure(CommonConfig::new);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, commonConfigForgeConfigSpecPair.getRight());

        commonConfig = commonConfigForgeConfigSpecPair.getLeft();
    }

    public CommonConfig getCommonConfig() {
        return commonConfig;
    }
}
