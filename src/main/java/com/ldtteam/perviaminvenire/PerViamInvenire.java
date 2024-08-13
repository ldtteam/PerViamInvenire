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
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(ModConstants.MOD_ID)
public class PerViamInvenire
{
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    public PerViamInvenire(IEventBus modBus)
    {
        PerViamInvenireApiProxy.getInstance().setApiInstance(new PerViamInvenireApiImplementation());

        NeoForge.EVENT_BUS.addListener((ServerStoppingEvent event) -> PathFinding.shutdown());
        modBus.addListener(this::initialize);
        modBus.addListener(this::registerCommandArgumentType);

        ConfigurationManager.getInstance().ensureInitialized(ModLoadingContext.get().getActiveContainer());
    }

    public void initialize(FMLCommonSetupEvent commonSetupEvent) {
        LOGGER.info("Starting PVI.");
        StartPositionAdapterInitializer.setup();
        VanillaCompatibilityManager.getInstance().initialize();
    }

    public void registerCommandArgumentType(final RegisterEvent registerEvent) {
        registerEvent.register(Registries.COMMAND_ARGUMENT_TYPE, helper -> {
            ArgumentTypeInfos.registerByClass(ImportableResultDataArgument.class, ImportableResultDataArgument.TypeInfo.getInstance());
            helper.register(ResourceLocation.fromNamespaceAndPath(ModConstants.MOD_ID, "pvi_importable_results"), ImportableResultDataArgument.TypeInfo.getInstance());
        });
    }

}
