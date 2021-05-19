package com.ldtteam.perviaminvenire.handlers;

import com.ldtteam.perviaminvenire.api.util.constants.ModConstants;
import net.minecraft.entity.monster.SpiderEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ModConstants.MOD_ID)
public class SpiderClimbableTickEventHandler
{

    @SubscribeEvent
    public static void onLivingUpdate(final LivingEvent.LivingUpdateEvent event)
    {
        if (!(event.getEntity() instanceof SpiderEntity)) {
            return;
        }

        final SpiderEntity spiderEntity = (SpiderEntity) event.getEntity();
        spiderEntity.setBesideClimbableBlock(false);
    }
}
