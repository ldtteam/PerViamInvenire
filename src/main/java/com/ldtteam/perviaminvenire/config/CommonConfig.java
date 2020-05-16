package com.ldtteam.perviaminvenire.config;

import com.ldtteam.perviaminvenire.api.config.ICommonConfig;
import net.minecraftforge.common.ForgeConfigSpec;

public class CommonConfig implements ICommonConfig {

    private final ForgeConfigSpec.IntValue minimumRailsToUseInPath;
    private final ForgeConfigSpec.IntValue maxPathFindingNodes;
    private final ForgeConfigSpec.IntValue pathFindingLogVerbosity;
    private final ForgeConfigSpec.IntValue pathFindingThreadCount;

    public CommonConfig(final ForgeConfigSpec.Builder builder) {
        this.minimumRailsToUseInPath = builder
                                           .comment("Minimum rails to use during pathing.")
                                           .translation("Minimum rails to use during pathing.")
                                           .defineInRange("minimumRailsToUseInPath", 5,5,100);
        this.maxPathFindingNodes = builder
                                                   .comment("Maximum amount of nodes used during a pathing calculation.")
                                                   .translation("Maximum amount of nodes used during a pathing calculation.")
                                                   .defineInRange("maxPathFindingNodes", 2500,5000,10000);
        this.pathFindingLogVerbosity = builder
                                                       .comment("Logging verbosity. Lower is more information.")
                                                       .translation("Logging verbosity. Lower is more information.")
                                                       .defineInRange("pathFindingLogVerbosity", 5,0,6);
        this.pathFindingThreadCount = builder
                                                   .comment("Amount of threads to use for PathFinding.")
                                                   .translation("Amount of threads to use for PathFinding.")
                                                   .defineInRange("pathFindingThreadCount", 5, 1, 256);
    }

    @Override
    public int getMinimumRailsToUseInPath() {
        return minimumRailsToUseInPath.get();
    }

    @Override
    public int getMaxPathFindingNodes() {
        return maxPathFindingNodes.get();
    }

    @Override
    public int getPathFindingLogVerbosity() {
        return pathFindingLogVerbosity.get();
    }

    @Override
    public int getPathFindingThreadingCount() {
        return pathFindingThreadCount.get();
    }
}
