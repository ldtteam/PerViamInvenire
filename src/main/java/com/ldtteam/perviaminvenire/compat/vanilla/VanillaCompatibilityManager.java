package com.ldtteam.perviaminvenire.compat.vanilla;

import com.ldtteam.perviaminvenire.api.adapters.registry.ISpeedAdaptationRegistry;
import com.ldtteam.perviaminvenire.api.config.ICommonConfig;
import com.ldtteam.perviaminvenire.api.pathfinding.IPathNavigatorProducer;
import com.ldtteam.perviaminvenire.api.pathfinding.registry.IPathNavigatorRegistry;
import com.ldtteam.perviaminvenire.api.util.ModTags;
import com.ldtteam.perviaminvenire.pathfinding.PerViamInvenireGroundPathNavigator;
import net.minecraft.entity.MobEntity;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.pathfinding.PathNavigator;

import java.util.Optional;

public class VanillaCompatibilityManager
{
    private static final VanillaCompatibilityManager INSTANCE = new VanillaCompatibilityManager();

    public static VanillaCompatibilityManager getInstance()
    {
        return INSTANCE;
    }

    private VanillaCompatibilityManager()
    {
    }

    public void initialize() {
        IPathNavigatorRegistry.getInstance().register(
          (mobEntity, initialNavigator) -> {
              if (!(ModTags.REPLACE_VANILLA_NAVIGATOR.contains(mobEntity.getType()) &&
                    ICommonConfig.getInstance().isVanillaReplacementEnabled() &&
                    !(initialNavigator instanceof PerViamInvenireGroundPathNavigator) &&
                    initialNavigator.getClass() == GroundPathNavigator.class))
                  return Optional.empty();

              final PerViamInvenireGroundPathNavigator navigator = new PerViamInvenireGroundPathNavigator(mobEntity, mobEntity.getEntityWorld());
              final GroundPathNavigator existingNavigator = (GroundPathNavigator) initialNavigator;
              navigator.getPathingOptions().setCanUseLadders(false);
              navigator.getPathingOptions().setCanSwim(existingNavigator.getNodeProcessor().getCanSwim());
              navigator.getPathingOptions().setCanUseRails(false);
              navigator.getPathingOptions().setCanOpenDoors(existingNavigator.getNodeProcessor().getCanOpenDoors());
              navigator.getPathingOptions().setEnterDoors(existingNavigator.getNodeProcessor().getCanEnterDoors());

              return Optional.of(navigator);
          }
        );

        ISpeedAdaptationRegistry.getInstance().register(
          (entity, walkSpeed) -> ModTags.REPLACE_VANILLA_NAVIGATOR.contains(entity.getType()) ? Optional.of(walkSpeed) : Optional.empty()
        );
    }
}
