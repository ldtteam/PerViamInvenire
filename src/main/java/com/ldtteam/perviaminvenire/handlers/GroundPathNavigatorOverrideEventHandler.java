package com.ldtteam.perviaminvenire.handlers;

import com.ldtteam.perviaminvenire.api.pathfinding.registry.IPathNavigateRegistry;
import com.ldtteam.perviaminvenire.api.util.ModTags;
import com.ldtteam.perviaminvenire.api.util.constants.ModConstants;
import com.ldtteam.perviaminvenire.pathfinding.PerViamInvenireGroundPathNavigate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ModConstants.MOD_ID)
public class GroundPathNavigatorOverrideEventHandler
{
    @SubscribeEvent
    public static void handleModSpawnNavigatorEvent(final EntityJoinWorldEvent event)
    {
        final Entity entity = event.getEntity();
        if (!(entity instanceof MobEntity))
            return;

        final MobEntity mob = (MobEntity) entity;
        mob.navigator = IPathNavigateRegistry.getInstance().getNavigateFor(mob);
    }
}
