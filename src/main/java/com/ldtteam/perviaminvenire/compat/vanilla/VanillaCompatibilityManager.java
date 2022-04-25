package com.ldtteam.perviaminvenire.compat.vanilla;

import com.ldtteam.perviaminvenire.api.adapters.registry.IIsLadderBlockRegistry;
import com.ldtteam.perviaminvenire.api.adapters.registry.ISpeedAdaptationRegistry;
import com.ldtteam.perviaminvenire.api.config.ICommonConfig;
import com.ldtteam.perviaminvenire.api.movement.registry.IMovementControllerRegistry;
import com.ldtteam.perviaminvenire.api.pathfinding.AbstractAdvancedGroundPathNavigator;
import com.ldtteam.perviaminvenire.api.pathfinding.IAdvancedPathNavigator;
import com.ldtteam.perviaminvenire.api.pathfinding.registry.IPathNavigatorRegistry;
import com.ldtteam.perviaminvenire.api.util.ModTags;
import com.ldtteam.perviaminvenire.movement.PVIMovementController;
import com.ldtteam.perviaminvenire.pathfinding.PerViamInvenireClimberPathNavigator;
import com.ldtteam.perviaminvenire.pathfinding.PerViamInvenireFlyingPathNavigator;
import com.ldtteam.perviaminvenire.pathfinding.PerViamInvenireGroundPathNavigator;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.monster.Slime;

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
              if (!(mobEntity.getType().is(ModTags.REPLACE_VANILLA_NAVIGATOR) &&
                    ICommonConfig.getInstance().isVanillaReplacementEnabled() &&
                    !(initialNavigator instanceof IAdvancedPathNavigator) &&
                    initialNavigator.getClass() == GroundPathNavigation.class))
                  return Optional.empty();

              final PerViamInvenireGroundPathNavigator navigator = new PerViamInvenireGroundPathNavigator(mobEntity, mobEntity.getCommandSenderWorld());
              final GroundPathNavigation existingNavigator = (GroundPathNavigation) initialNavigator;
              navigator.getPathingOptions().setCanUseLadders(false);
              navigator.getPathingOptions().setCanSwim(existingNavigator.getNodeEvaluator().canFloat());
              navigator.getPathingOptions().setCanUseRails(false);
              navigator.getPathingOptions().setCanOpenDoors(existingNavigator.getNodeEvaluator().canOpenDoors());
              navigator.getPathingOptions().setEnterDoors(existingNavigator.getNodeEvaluator().canPassDoors());

              return Optional.of(navigator);
          }
        );

        IPathNavigatorRegistry.getInstance().register(
          (mobEntity, initialNavigator) -> {
              if (!(mobEntity.getType().is(ModTags.REPLACE_VANILLA_NAVIGATOR) &&
                      ICommonConfig.getInstance().isVanillaReplacementEnabled() &&
                      !(initialNavigator instanceof IAdvancedPathNavigator) &&
                      initialNavigator.getClass() == WallClimberNavigation.class))
                  return Optional.empty();

              final PerViamInvenireClimberPathNavigator navigator = new PerViamInvenireClimberPathNavigator(mobEntity, mobEntity.getCommandSenderWorld());
              final WallClimberNavigation existingNavigator = (WallClimberNavigation) initialNavigator;
              navigator.getPathingOptions().setCanUseLadders(true);
              navigator.getPathingOptions().setCanSwim(existingNavigator.getNodeEvaluator().canFloat());
              navigator.getPathingOptions().setCanUseRails(false);
              navigator.getPathingOptions().setCanOpenDoors(existingNavigator.getNodeEvaluator().canOpenDoors());
              navigator.getPathingOptions().setEnterDoors(existingNavigator.getNodeEvaluator().canPassDoors());

              return Optional.of(navigator);
          }
        );

        IPathNavigatorRegistry.getInstance().register(
          (mobEntity, initialNavigator) -> {
              if (!(mobEntity.getType().is(ModTags.REPLACE_VANILLA_NAVIGATOR) &&
                      ICommonConfig.getInstance().isVanillaReplacementEnabled() &&
                      !(initialNavigator instanceof IAdvancedPathNavigator) &&
                      initialNavigator.getClass() == FlyingPathNavigation.class))
                  return Optional.empty();

              final PerViamInvenireFlyingPathNavigator navigator = new PerViamInvenireFlyingPathNavigator(mobEntity, mobEntity.getCommandSenderWorld());
              final FlyingPathNavigation existingNavigator = (FlyingPathNavigation) initialNavigator;
              navigator.getPathingOptions().setCanUseLadders(false);
              navigator.getPathingOptions().setCanSwim(existingNavigator.getNodeEvaluator().canFloat());
              navigator.getPathingOptions().setCanUseRails(false);
              navigator.getPathingOptions().setCanOpenDoors(existingNavigator.getNodeEvaluator().canOpenDoors());
              navigator.getPathingOptions().setEnterDoors(existingNavigator.getNodeEvaluator().canPassDoors());

              return Optional.of(navigator);
          }
        );

        IMovementControllerRegistry.getInstance().register((entity, initialController) -> {
            if (entity instanceof Slime || !(entity.navigation instanceof AbstractAdvancedGroundPathNavigator))
                return Optional.empty();

            return Optional.of(new PVIMovementController(entity));
        });

        ISpeedAdaptationRegistry.getInstance().register(
          (entity, walkSpeed) -> entity.getType().is(ModTags.REPLACE_VANILLA_NAVIGATOR) ? Optional.of(walkSpeed) : Optional.empty()
        );

        IIsLadderBlockRegistry.getInstance().register((entity, block, worldReader, blockPos) -> {
            if (!(entity instanceof final Mob mobEntity))
                return Optional.empty();

            if (!(mobEntity.getNavigation() instanceof PerViamInvenireClimberPathNavigator))
                return Optional.empty();

            return Optional.of(block.isAir() && Direction.Plane.HORIZONTAL.stream()
              .anyMatch(direction -> !worldReader.getBlockState(blockPos.offset(direction.getNormal())).isAir()));
        });
    }
}
