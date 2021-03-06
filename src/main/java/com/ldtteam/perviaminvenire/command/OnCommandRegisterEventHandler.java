package com.ldtteam.perviaminvenire.command;

import com.ldtteam.perviaminvenire.api.util.constants.ModConstants;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ModConstants.MOD_ID)
public class OnCommandRegisterEventHandler
{

    @SubscribeEvent
    public static void onRegisterCommands(final RegisterCommandsEvent event)
    {
        PerViamInvenireCommand.getInstance().register(event.getDispatcher());
    }
}
