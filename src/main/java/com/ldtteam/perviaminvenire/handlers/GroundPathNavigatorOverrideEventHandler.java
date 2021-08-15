package com.ldtteam.perviaminvenire.handlers;

import com.ldtteam.perviaminvenire.api.util.constants.ModConstants;
import com.ldtteam.perviaminvenire.pathfinding.registry.MovementControllerRegistry;
import com.ldtteam.perviaminvenire.pathfinding.registry.PathNavigatorRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.controller.MovementController;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = ModConstants.MOD_ID)
public class GroundPathNavigatorOverrideEventHandler
{
    private static final Logger LOGGER = LogManager.getLogger();

    private static Field movementControllerField = ObfuscationReflectionHelper.findField(
      MobEntity.class, "field_70765_h"
    );

    @SubscribeEvent
    public static void handleModSpawnNavigatorEvent(final EntityJoinWorldEvent event)
    {
        final Entity entity = event.getEntity();
        if (!(entity instanceof MobEntity))
            return;

        final MobEntity mob = (MobEntity) entity;
        final Optional<PathNavigator> overrideHandler = PathNavigatorRegistry.getInstance().getRunner().get(mob, mob.getNavigation());
        overrideHandler.ifPresent(pathNavigator -> mob.navigation = pathNavigator);

        final Optional<MovementController> controllerHandler = MovementControllerRegistry.getInstance().getRunner().get(mob, mob.getMoveControl());
        controllerHandler.ifPresent(controller -> {
            try
            {
                movementControllerField.set(mob, controller);
            }
            catch (IllegalAccessException e)
            {
               LOGGER.warn("Failed to update the movement controller of an entity.", e);
            }
        });
    }
}
