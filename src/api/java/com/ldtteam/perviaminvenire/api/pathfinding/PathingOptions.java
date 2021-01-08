package com.ldtteam.perviaminvenire.api.pathfinding;

/**
 * Configuration values for pathing, used by pathjobs and normally set through the navigator
 */
public class PathingOptions
{
    /**
     * Additional cost of jumping and dropping - base 1.
     */
    public double jumpDropCost = 2.0D;

    /**
     * Cost improvement of paths - base 1.
     */
    public double onPathCost = 0.1D;

    /**
     * Cost improvement of ladders - base 1.
     */
    public double onLadderCost = 1D;

    /**
     * Cost improvement of paths - base 1.
     */
    public double onRailCost = 0.01D;

    /**
     * The rails exit cost.
     */
    public double railsExitCost = 5;

    /**
     * Additional cost of swimming - base 1.
     */
    public double swimCost = 1.5D;

    /**
     * Additional cost enter entering water
     */
    public double swimCostEnter = 25D;

    /**
     * Wether to use ladders during pathing.
     */
    private boolean canUseLadders = false;

    /**
     * Whether to use minecart rail pathing
     */
    private boolean canUseRails  = false;
    /**
     * Can swim
     */
    private boolean canSwim      = false;
    /**
     * Allowed to enter doors?
     */
    private boolean enterDoors   = false;
    /**
     * Allowed to open doors?
     */
    private boolean canOpenDoors = false;

    public PathingOptions()
    {}

    public boolean canOpenDoors()
    {
        return canOpenDoors;
    }

    public void setCanOpenDoors(final boolean canOpenDoors)
    {
        this.canOpenDoors = canOpenDoors;
    }

    public boolean canUseLadders()
    {
        return canUseLadders;
    }

    public void setCanUseLadders(final boolean canUseLadders)
    {
        this.canUseLadders = canUseLadders;
    }

    public boolean canUseRails()
    {
        return canUseRails;
    }

    public void setCanUseRails(final boolean canUseRails)
    {
        this.canUseRails = canUseRails;
    }

    public boolean canSwim()
    {
        return canSwim;
    }

    public void setCanSwim(final boolean canSwim)
    {
        this.canSwim = canSwim;
    }

    public boolean canUseDynamicPassableBlocks()
    {
        return enterDoors;
    }

    public void setEnterDoors(final boolean enterDoors)
    {
        this.enterDoors = enterDoors;
    }

    public PathingOptions withStartSwimCost(final double startSwimCost)
    {
        swimCostEnter = startSwimCost;
        return this;
    }

    public PathingOptions withSwimCost(final double swimCost)
    {
        this.swimCost = swimCost;
        return this;
    }

    public PathingOptions withJumpDropCost(final double jumpDropCost)
    {
        this.jumpDropCost = jumpDropCost;
        return this;
    }

    public PathingOptions withOnPathCost(final double onPathCost)
    {
        this.onPathCost = onPathCost;
        return this;
    }

    public PathingOptions withOnRailCost(final double onRailCost)
    {
        this.onRailCost = onRailCost;
        return this;
    }

    public PathingOptions withRailExitCost(final double railExitCost)
    {
        railsExitCost = railExitCost;
        return this;
    }

    public PathingOptions withLadderCost(final double onLadderCost)
    {
        this.onLadderCost = onLadderCost;
        return this;
    }
}
