package com.ldtteam.perviaminvenire.api.pathfinding;

import com.ldtteam.perviaminvenire.api.pathfinding.stuckhandling.IStuckHandler;
import net.minecraft.entity.MobEntity;
import net.minecraft.pathfinding.FlyingPathNavigator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractAdvancedFlyingPathNavigator extends FlyingPathNavigator implements IAdvancedPathNavigator
{
    //  Parent class private members
    protected final MobEntity    ourEntity;
    @Nullable
    protected       BlockPos     destination;
    protected       double       walkSpeed = 1.0D;
    protected       double       requestedSpeed = walkSpeed;
    @Nullable
    protected       BlockPos     originalDestination;

    /**
     * The navigators node costs
     */
    private PathingOptions pathingOptions = new PathingOptions();

    public AbstractAdvancedFlyingPathNavigator(final MobEntity entity, final World world)
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
    public MobEntity getOurEntity()
    {
        return ourEntity;
    }
}
