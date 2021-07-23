package com.ldtteam.perviaminvenire.handlers;

import com.ldtteam.perviaminvenire.api.pathfinding.AbstractAdvancedGroundPathNavigator;
import com.ldtteam.perviaminvenire.api.pathfinding.PathPointExtended;
import com.ldtteam.perviaminvenire.api.util.constants.ModConstants;
import net.minecraft.entity.monster.SpiderEntity;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
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

        if (!(spiderEntity.getNavigation() instanceof AbstractAdvancedGroundPathNavigator))
            return;

        final AbstractAdvancedGroundPathNavigator navigator = (AbstractAdvancedGroundPathNavigator) spiderEntity.getNavigation();
        if (navigator.isInProgress() || navigator.getPath() == null) {
            spiderEntity.setClimbing(false);
            return;
        }

        final Path path = navigator.getPath();
        final PathPoint pathPoint = path.getNextNode();
        if (!(pathPoint instanceof PathPointExtended)) {
            spiderEntity.setClimbing(false);
            return;
        }

        final PathPointExtended pathPointExtended = (PathPointExtended) pathPoint;
        final boolean currentIsLadder = pathPointExtended.isOnLadder();
        if (path.getNextNodeIndex() == 0)
        {
            spiderEntity.setClimbing(currentIsLadder);
            return;
        }

        final PathPoint pathPointPrevious = path.getNode(path.getNextNodeIndex() - 1);
        if (!(pathPointPrevious instanceof PathPointExtended)) {
            spiderEntity.setClimbing(currentIsLadder);
            return;
        }
        final PathPointExtended pathPointPreviousExtended = (PathPointExtended) pathPoint;
        final boolean previousIsLadder = pathPointPreviousExtended.isOnLadder();

        spiderEntity.setClimbing(previousIsLadder || currentIsLadder);
    }
}
