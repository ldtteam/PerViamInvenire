package com.ldtteam.perviaminvenire.compat.vanilla;

import com.ldtteam.perviaminvenire.api.util.ModTags;
import com.ldtteam.perviaminvenire.pathfinding.PerViamInvenireGroundPathNavigate;
import com.ldtteam.perviaminvenire.api.util.constants.ModConstants;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Objects;

@Mod.EventBusSubscriber(modid = ModConstants.MOD_ID)
public class VanillaMobSpawnEventHandler
{

    @SubscribeEvent
    public static void handleModSpawnNavigatorEvent(final LivingSpawnEvent event)
    {
        final LivingEntity entity = event.getEntityLiving();
        if (!(entity instanceof MobEntity))
            return;

        final MobEntity mob = (MobEntity) entity;
        if (!ModTags.REPLACE_VANILLA_NAVIGATOR.contains(mob.getType()))
            return;

        mob.navigator = new PerViamInvenireGroundPathNavigate(
          mob,
          mob.getEntityWorld()
        );
    }
}
