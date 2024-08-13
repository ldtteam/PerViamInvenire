package com.ldtteam.perviaminvenire.compat.vanilla;

import com.ldtteam.perviaminvenire.api.compat.vanilla.ICompatibilityPathingOptions;
import com.ldtteam.perviaminvenire.api.pathfinding.PathingOptions;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.PathType;

public class VanillaCompatibilityPathingOptions extends PathingOptions implements ICompatibilityPathingOptions {

    private final Mob mob;
    private final NodeEvaluator legacyNodeEvaluator;

    public VanillaCompatibilityPathingOptions(Mob mob) {
        this.mob = mob;
        this.legacyNodeEvaluator = mob.getNavigation().getNodeEvaluator();
    }

    @Override
    public boolean canOpenDoors() {
        return legacyNodeEvaluator.canOpenDoors();
    }

    @Override
    public boolean canUseLadders() {
        return false;
    }

    @Override
    public boolean canUseRails() {
        return false;
    }

    @Override
    public boolean canFloat() {
        return legacyNodeEvaluator.canFloat();
    }

    @Override
    public boolean canSwim() {
        return mob.getPathfindingMalus(PathType.WATER) >= 0.0F;
    }

    @Override
    public double onPathCost() {
        return mob.getPathfindingMalus(PathType.WALKABLE);
    }

    @Override
    public double swimCost() {
        return mob.getPathfindingMalus(PathType.WATER);
    }

    @Override
    public double swimCostEnter() {
        return mob.getPathfindingMalus(PathType.WATER_BORDER);
    }
}
