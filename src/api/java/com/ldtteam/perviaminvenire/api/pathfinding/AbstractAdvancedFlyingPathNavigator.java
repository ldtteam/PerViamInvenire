package com.ldtteam.perviaminvenire.api.pathfinding;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractAdvancedFlyingPathNavigator extends FlyingPathNavigation implements IAdvancedPathNavigator
{
    //  Parent class private members
    protected final Mob      ourEntity;
    @Nullable
    protected       BlockPos destination;
    protected       double   walkSpeed = 1.0D;
    protected       double       requestedSpeed = walkSpeed;
    @Nullable
    protected       BlockPos     originalDestination;

    /**
     * The navigators node costs
     */
    private PathingOptions pathingOptions = new PathingOptions();

    public AbstractAdvancedFlyingPathNavigator(final Mob entity, final Level world)
    {
        super(entity, world);
        this.ourEntity = entity;
    }

    @Override
    public @Nullable BlockPos getDestination()
    {
        return destination;
    }

    @Override
    public PathingOptions getPathingOptions()
    {
        return pathingOptions;
    }

    @Override
    public void setPathingOptions(PathingOptions pathingOptions) {
        this.pathingOptions = pathingOptions;
    }

    @Override
    public Mob getOurEntity()
    {
        return ourEntity;
    }

    @Override
    public @Nullable Path getCurrentPath()
    {
        return getPath();
    }
}
