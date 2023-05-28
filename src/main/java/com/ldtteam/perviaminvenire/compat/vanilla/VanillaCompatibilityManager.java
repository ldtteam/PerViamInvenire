package com.ldtteam.perviaminvenire.compat.vanilla;

import com.ldtteam.perviaminvenire.api.adapters.registry.IIsLadderBlockRegistry;
import com.ldtteam.perviaminvenire.api.adapters.registry.IPassableBlockRegistry;
import com.ldtteam.perviaminvenire.api.adapters.registry.ISpeedAdaptationRegistry;
import com.ldtteam.perviaminvenire.api.config.ICommonConfig;
import com.ldtteam.perviaminvenire.api.movement.registry.IMovementControllerRegistry;
import com.ldtteam.perviaminvenire.api.movement.registry.IWantedMovementHandlerRegistry;
import com.ldtteam.perviaminvenire.api.pathfinding.AbstractAdvancedGroundPathNavigator;
import com.ldtteam.perviaminvenire.api.pathfinding.IAdvancedPathNavigator;
import com.ldtteam.perviaminvenire.api.pathfinding.registry.IPathNavigatorRegistry;
import com.ldtteam.perviaminvenire.api.util.ModTags;
import com.ldtteam.perviaminvenire.movement.PVIMovementController;
import com.ldtteam.perviaminvenire.pathfinding.PerViamInvenireClimberPathNavigator;
import com.ldtteam.perviaminvenire.pathfinding.PerViamInvenireFlyingPathNavigator;
import com.ldtteam.perviaminvenire.pathfinding.PerViamInvenireGroundPathNavigator;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.phys.Vec3;

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
              navigator.setPathingOptions(new VanillaCompatibilityPathingOptions(mobEntity));

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
              navigator.setPathingOptions(new VanillaCompatibilityPathingOptions(mobEntity));

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
              navigator.setPathingOptions(new VanillaCompatibilityPathingOptions(mobEntity));

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

        IPassableBlockRegistry.getInstance().register((entity, block) -> {
            if (entity.getType().is(ModTags.REPLACE_VANILLA_NAVIGATOR)) {
                if (entity instanceof WaterAnimal) {
                    return Optional.of(block.getFluidState().is(FluidTags.WATER));
                }

                return Optional.of(block.isAir());
            }

            return Optional.empty();
        });

        IWantedMovementHandlerRegistry.getInstance().register((entity, x, y, z, speed) -> {
            if (!(entity instanceof Squid squid))
                return false;

            final Vec3 requested = new Vec3(x,y,z);
            final Vec3 delta = requested.subtract(entity.position());

            squid.setMovementVector((float) delta.x, (float) delta.y, (float) delta.z);
            squid.setSpeed((float) speed);
            return true;
        });
    }
}
