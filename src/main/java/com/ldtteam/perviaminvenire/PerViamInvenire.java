package com.ldtteam.perviaminvenire;

import com.ldtteam.perviaminvenire.api.PerViamInvenireApiProxy;
import com.ldtteam.perviaminvenire.api.util.constants.ModConstants;
import com.ldtteam.perviaminvenire.apiimpl.PerViamInvenireApiImplementation;
import com.ldtteam.perviaminvenire.command.ImportableResultDataArgument;
import com.ldtteam.perviaminvenire.compat.vanilla.VanillaCompatibilityManager;
import com.ldtteam.perviaminvenire.config.ConfigurationManager;
import com.ldtteam.perviaminvenire.network.NetworkManager;
import com.ldtteam.perviaminvenire.pathfinding.PathFinding;
import com.ldtteam.perviaminvenire.pathfinding.initialization.StartPositionAdapterInitializer;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(ModConstants.MOD_ID)
public class PerViamInvenire
{
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    public PerViamInvenire()
    {
        PerViamInvenireApiProxy.getInstance().setApiInstance(new PerViamInvenireApiImplementation());

        Mod.EventBusSubscriber.Bus.FORGE.bus().get().addListener((ServerStoppingEvent event) -> PathFinding.shutdown());
        Mod.EventBusSubscriber.Bus.MOD.bus().get().addListener(this::initialize);
        Mod.EventBusSubscriber.Bus.MOD.bus().get().addListener(this::registerCommandArgumentType);

        NetworkManager.getInstance().initialize();
        ConfigurationManager.getInstance().ensureInitialized(ModLoadingContext.get().getActiveContainer());
    }

    public void initialize(FMLCommonSetupEvent commonSetupEvent) {
        LOGGER.info("Starting PVI.");
        StartPositionAdapterInitializer.setup();
        VanillaCompatibilityManager.getInstance().initialize();
    }

    public void registerCommandArgumentType(final RegisterEvent registerEvent) {
        registerEvent.register(ForgeRegistries.Keys.COMMAND_ARGUMENT_TYPES, helper -> {
            ArgumentTypeInfos.registerByClass(ImportableResultDataArgument.class, ImportableResultDataArgument.TypeInfo.getInstance());
            helper.register("pvi_importable_results", ImportableResultDataArgument.TypeInfo.getInstance());
        });
    }

}
