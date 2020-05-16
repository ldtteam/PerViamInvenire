package com.ldtteam.perviaminvenire;

import com.ldtteam.perviaminvenire.pathfinding.PathFinding;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("per-viam-invenire")
public class PerViamInvenire
{

    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    public PerViamInvenire()
    {
        Mod.EventBusSubscriber.Bus.FORGE.bus().get().addListener((FMLServerStartingEvent event) -> {
            PathFinding.shutdown();
        });
    }

}
