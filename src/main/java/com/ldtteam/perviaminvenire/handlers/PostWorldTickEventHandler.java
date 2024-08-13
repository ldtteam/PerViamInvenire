package com.ldtteam.perviaminvenire.handlers;

import com.ldtteam.perviaminvenire.api.results.ICalculationResultsImportManager;
import com.ldtteam.perviaminvenire.api.util.constants.ModConstants;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = ModConstants.MOD_ID)
public class PostWorldTickEventHandler
{

    @SubscribeEvent
    public static void onTickWorldTick(final LevelTickEvent.Post event)
    {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            ICalculationResultsImportManager.getInstance().onPostWorldTick(serverLevel);
        }
    }
}
