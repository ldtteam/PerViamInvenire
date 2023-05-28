package com.ldtteam.perviaminvenire.api.pathfinding;

/**
 * Configuration values for pathing, used by pathjobs and normally set through the navigator
 */
public class PathingOptions
{
    private double jumpDropCost = 2.0D;

    private double onPathCost = 0.1D;

    private double onLadderCost = 1D;

    private double onRailCost = 0.01D;

    private double railsExitCost = 5;

    private double swimCost = 1.5D;

    private double swimCostEnter = 25D;

    private double traverseToggleAbleCost = 2D;

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
    /**
     * Allowed to float?
     */
    private boolean canFloat = false;

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

    public boolean canFloat() {
        return canFloat;
    }

    public void setCanFloat(boolean canFloat) {
        this.canFloat = canFloat;
    }

    public PathingOptions withStartSwimCost(final double startSwimCost)
    {
        setSwimCostEnter(startSwimCost);
        return this;
    }

    public PathingOptions withSwimCost(final double swimCost)
    {
        this.setSwimCost(swimCost);
        return this;
    }

    public PathingOptions withJumpDropCost(final double jumpDropCost)
    {
        this.setJumpDropCost(jumpDropCost);
        return this;
    }

    public PathingOptions withOnPathCost(final double onPathCost)
    {
        this.setOnPathCost(onPathCost);
        return this;
    }

    public PathingOptions withOnRailCost(final double onRailCost)
    {
        this.setOnRailCost(onRailCost);
        return this;
    }

    public PathingOptions withRailExitCost(final double railExitCost)
    {
        setRailsExitCost(railExitCost);
        return this;
    }

    public PathingOptions withLadderCost(final double onLadderCost)
    {
        this.setOnLadderCost(onLadderCost);
        return this;
    }

    public PathingOptions withToggleCost(final double toggleCost)
    {
        setTraverseToggleAbleCost(toggleCost);
        return this;
    }

    /**
     * Additional cost of jumping and dropping - base 1.
     */
    public double jumpDropCost() {
        return jumpDropCost;
    }

    public void setJumpDropCost(double jumpDropCost) {
        this.jumpDropCost = jumpDropCost;
    }

    /**
     * Cost improvement of paths - base 1.
     */
    public double onPathCost() {
        return onPathCost;
    }

    public void setOnPathCost(double onPathCost) {
        this.onPathCost = onPathCost;
    }

    /**
     * Cost improvement of ladders - base 1.
     */
    public double onLadderCost() {
        return onLadderCost;
    }

    public void setOnLadderCost(double onLadderCost) {
        this.onLadderCost = onLadderCost;
    }

    /**
     * Cost improvement of paths - base 1.
     */
    public double onRailCost() {
        return onRailCost;
    }

    public void setOnRailCost(double onRailCost) {
        this.onRailCost = onRailCost;
    }

    /**
     * The rails exit cost.
     */
    public double railsExitCost() {
        return railsExitCost;
    }

    public void setRailsExitCost(double railsExitCost) {
        this.railsExitCost = railsExitCost;
    }

    /**
     * Additional cost of swimming - base 1.
     */
    public double swimCost() {
        return swimCost;
    }

    public void setSwimCost(double swimCost) {
        this.swimCost = swimCost;
    }

    /**
     * Additional cost enter entering water
     */
    public double swimCostEnter() {
        return swimCostEnter;
    }

    public void setSwimCostEnter(double swimCostEnter) {
        this.swimCostEnter = swimCostEnter;
    }

    /**
     * Cost to traverse trap doors
     */
    public double traverseToggleAbleCost() {
        return traverseToggleAbleCost;
    }

    public void setTraverseToggleAbleCost(double traverseToggleAbleCost) {
        this.traverseToggleAbleCost = traverseToggleAbleCost;
    }
}
