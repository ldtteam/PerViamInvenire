package com.ldtteam.perviaminvenire.command;

import com.ldtteam.perviaminvenire.api.util.constants.ModConstants;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = ModConstants.MOD_ID)
public class OnCommandRegisterEventHandler
{

    @SubscribeEvent
    public static void onRegisterCommands(final RegisterCommandsEvent event)
    {
        PerViamInvenireCommand.getInstance().register(event.getDispatcher());
    }
}
