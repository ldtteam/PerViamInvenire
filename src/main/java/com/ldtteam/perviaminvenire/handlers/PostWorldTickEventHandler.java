package com.ldtteam.perviaminvenire.handlers;

import com.ldtteam.perviaminvenire.api.results.ICalculationResultsImportManager;
import com.ldtteam.perviaminvenire.api.util.constants.ModConstants;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ModConstants.MOD_ID)
public class PostWorldTickEventHandler
{

    @SubscribeEvent
    public static void onTickWorldTick(final TickEvent.WorldTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END && event.world instanceof ServerLevel) {
            ICalculationResultsImportManager.getInstance().onPostWorldTick((ServerLevel) event.world);
        }
    }
}
