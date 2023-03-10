package com.ldtteam.perviaminvenire.api.pathfinding;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractAdvancedGroundPathNavigator extends GroundPathNavigation implements IAdvancedPathNavigator {
    //  Parent class private members
    protected final Mob ourEntity;
    @Nullable
    protected BlockPos destination;
    protected double walkSpeed = 1.0D;
    protected double requestedSpeed = walkSpeed;
    @Nullable
    protected BlockPos originalDestination;

    /**
     * The navigators node costs
     */
    private PathingOptions pathingOptions = new PathingOptions();

    public AbstractAdvancedGroundPathNavigator(
            final Mob entityLiving,
            final Level worldIn) {
        super(entityLiving, worldIn);
        this.ourEntity = mob;
    }

    /**
     * Get the destination from the path.
     *
     * @return the destination position.
     */
    @Override
    @Nullable
    public BlockPos getDestination() {
        return destination;
    }

    /**
     * Get the pathing options
     *
     * @return the pathing options.
     */
    @Override
    public PathingOptions getPathingOptions() {
        return pathingOptions;
    }

    /**
     * Get the entity of this navigator
     *
     * @return mobentity
     */
    @Override
    public Mob getOurEntity() {
        return ourEntity;
    }

    @Override
    public @Nullable Path getCurrentPath() {
        return getPath();
    }
}
