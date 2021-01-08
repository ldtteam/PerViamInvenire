package com.ldtteam.perviaminvenire.compat.vanilla;

import com.ldtteam.perviaminvenire.api.adapters.registry.ISpeedAdaptationRegistry;
import com.ldtteam.perviaminvenire.api.config.ICommonConfig;
import com.ldtteam.perviaminvenire.api.pathfinding.registry.IPathNavigateRegistry;
import com.ldtteam.perviaminvenire.api.util.ModTags;
import com.ldtteam.perviaminvenire.pathfinding.PerViamInvenireGroundPathNavigate;
import net.minecraft.pathfinding.GroundPathNavigator;

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
        IPathNavigateRegistry.getInstance().registerNewPathNavigate(
          mobEntity -> ModTags.REPLACE_VANILLA_NAVIGATOR.contains(mobEntity.getType()) &&
                         ICommonConfig.getInstance().isVanillaReplacementEnabled() &&
                         !(mobEntity.getNavigator() instanceof PerViamInvenireGroundPathNavigate) &&
                        mobEntity.getNavigator().getClass() == GroundPathNavigator.class,
          mobEntity -> {
              final PerViamInvenireGroundPathNavigate navigator = new PerViamInvenireGroundPathNavigate(mobEntity, mobEntity.getEntityWorld());
              if (mobEntity.getNavigator() instanceof GroundPathNavigator) {
                  final GroundPathNavigator existingNavigator = (GroundPathNavigator) mobEntity.getNavigator();

                  navigator.getPathingOptions().setCanUseLadders(false);
                  navigator.getPathingOptions().setCanSwim(existingNavigator.getNodeProcessor().getCanSwim());
                  navigator.getPathingOptions().setCanUseRails(false);
                  navigator.getPathingOptions().setCanOpenDoors(existingNavigator.getNodeProcessor().getCanOpenDoors());
                  navigator.getPathingOptions().setEnterDoors(existingNavigator.getNodeProcessor().getCanEnterDoors());
              }

              return navigator;
          }
        );

        ISpeedAdaptationRegistry.getInstance().register(
          (entity, walkSpeed) -> ModTags.REPLACE_VANILLA_NAVIGATOR.contains(entity.getType()) ? Optional.of(walkSpeed) : Optional.empty()
        );
    }
}
