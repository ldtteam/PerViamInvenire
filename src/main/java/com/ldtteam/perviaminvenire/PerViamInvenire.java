package com.ldtteam.perviaminvenire;

import com.ldtteam.perviaminvenire.api.PerViamInvenireApiProxy;
import com.ldtteam.perviaminvenire.api.adapters.registry.ISpeedAdaptationRegistry;
import com.ldtteam.perviaminvenire.api.adapters.speed.ISpeedAdaptationCallback;
import com.ldtteam.perviaminvenire.api.util.ModTags;
import com.ldtteam.perviaminvenire.apiimpl.PerViamInvenireApiImplementation;
import com.ldtteam.perviaminvenire.compat.vanilla.VanillaCompatibilityManager;
import com.ldtteam.perviaminvenire.config.ConfigurationManager;
import com.ldtteam.perviaminvenire.pathfinding.PathFinding;
import com.ldtteam.perviaminvenire.api.util.constants.ModConstants;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(ModConstants.MOD_ID)
public class PerViamInvenire
{
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    public PerViamInvenire()
    {
        PerViamInvenireApiProxy.getInstance().setApiInstance(new PerViamInvenireApiImplementation());

        Mod.EventBusSubscriber.Bus.FORGE.bus().get().addListener((FMLServerStoppingEvent event) -> {
            PathFinding.shutdown();
        });
        Mod.EventBusSubscriber.Bus.MOD.bus().get().addListener(this::initialize);

        ConfigurationManager.getInstance().ensureInitialized(ModLoadingContext.get().getActiveContainer());
    }

    public void initialize(FMLCommonSetupEvent commonSetupEvent) {
        LOGGER.info("Starting PVI.");
        VanillaCompatibilityManager.getInstance().initialize();
    }

}
