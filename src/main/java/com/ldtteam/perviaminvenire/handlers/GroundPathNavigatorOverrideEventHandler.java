package com.ldtteam.perviaminvenire.handlers;

import com.ldtteam.perviaminvenire.api.pathfinding.registry.IPathNavigatorRegistry;
import com.ldtteam.perviaminvenire.api.util.constants.ModConstants;
import com.ldtteam.perviaminvenire.pathfinding.registry.PathNavigatorRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

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
        final Optional<PathNavigator> overrideHandler = PathNavigatorRegistry.getInstance().getRunner().get(mob, mob.getNavigator());
        overrideHandler.ifPresent(pathNavigator -> mob.navigator = pathNavigator);
    }
}
