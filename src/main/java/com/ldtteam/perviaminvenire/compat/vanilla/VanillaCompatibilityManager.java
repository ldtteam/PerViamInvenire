package com.ldtteam.perviaminvenire.compat.vanilla;

import com.ldtteam.perviaminvenire.api.adapters.ladder.IIsLadderBlockCallback;
import com.ldtteam.perviaminvenire.api.adapters.registry.IIsLadderBlockRegistry;
import com.ldtteam.perviaminvenire.api.adapters.registry.ISpeedAdaptationRegistry;
import com.ldtteam.perviaminvenire.api.config.ICommonConfig;
import com.ldtteam.perviaminvenire.api.pathfinding.IAdvancedPathNavigator;
import com.ldtteam.perviaminvenire.api.pathfinding.IPathNavigatorProducer;
import com.ldtteam.perviaminvenire.api.pathfinding.registry.IPathNavigatorRegistry;
import com.ldtteam.perviaminvenire.api.util.ModTags;
import com.ldtteam.perviaminvenire.pathfinding.PerViamInvenireClimberPathNavigator;
import com.ldtteam.perviaminvenire.pathfinding.PerViamInvenireGroundPathNavigator;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.pathfinding.ClimberPathNavigator;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

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
                    !(initialNavigator instanceof IAdvancedPathNavigator) &&
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

        IPathNavigatorRegistry.getInstance().register(
          (mobEntity, initialNavigator) -> {
              if (!(ModTags.REPLACE_VANILLA_NAVIGATOR.contains(mobEntity.getType()) &&
                      ICommonConfig.getInstance().isVanillaReplacementEnabled() &&
                      !(initialNavigator instanceof IAdvancedPathNavigator) &&
                      initialNavigator.getClass() == ClimberPathNavigator.class))
                  return Optional.empty();

              final PerViamInvenireClimberPathNavigator navigator = new PerViamInvenireClimberPathNavigator(mobEntity, mobEntity.getEntityWorld());
              final ClimberPathNavigator existingNavigator = (ClimberPathNavigator) initialNavigator;
              navigator.getPathingOptions().setCanUseLadders(true);
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

        IIsLadderBlockRegistry.getInstance().register((entity, block, worldReader, blockPos) -> {
            if (!(entity instanceof MobEntity))
                return Optional.empty();

            final MobEntity mobEntity = (MobEntity) entity;
            if (!(mobEntity.getNavigator() instanceof PerViamInvenireClimberPathNavigator))
                return Optional.empty();

            return Optional.of(Direction.Plane.HORIZONTAL.getDirectionValues()
              .anyMatch(direction -> !worldReader.getBlockState(blockPos.add(direction.getDirectionVec())).isAir(worldReader, blockPos.add(direction.getDirectionVec()))));
        });
    }
}
