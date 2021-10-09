package com.ldtteam.perviaminvenire.api.pathfinding.stuckhandling;

import com.ldtteam.perviaminvenire.api.pathfinding.IAdvancedPathNavigator;

/**
 * Stuck handler for pathing, gets called to check/deal with stuck status
 */
public interface IStuckHandler
{
    /**
     * Checks if the navigator is stuck
     *
     * @param navigator navigator to check
     */
    void checkStuck(final IAdvancedPathNavigator navigator);
}
