package com.ldtteam.perviaminvenire.handlers;

import com.ldtteam.perviaminvenire.api.util.constants.ModConstants;
import com.ldtteam.perviaminvenire.pathfinding.registry.MovementControllerRegistry;
import com.ldtteam.perviaminvenire.pathfinding.registry.PathNavigatorRegistry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

import java.util.Optional;

@EventBusSubscriber(modid = ModConstants.MOD_ID)
public class GroundPathNavigatorOverrideEventHandler
{
    @SubscribeEvent
    public static void handleModSpawnNavigatorEvent(final EntityJoinLevelEvent event)
    {
        final Entity entity = event.getEntity();
        if (!(entity instanceof final Mob mob))
            return;

        final Optional<PathNavigation> overrideHandler = PathNavigatorRegistry.getInstance().getRunner().get(mob, mob.getNavigation());
        overrideHandler.ifPresent(pathNavigator -> mob.navigation = pathNavigator);

        final Optional<MoveControl> controllerHandler = MovementControllerRegistry.getInstance().getRunner().get(mob, mob.getMoveControl());
        controllerHandler.ifPresent(controller -> {
            mob.moveControl = controller;
        });
    }
}
