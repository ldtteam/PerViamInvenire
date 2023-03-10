package com.ldtteam.perviaminvenire.util;

import com.ldtteam.perviaminvenire.api.pathfinding.CalculationNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import org.jetbrains.annotations.NotNull;

public final class CalculationNodeUtils {

    public static final float DESTINATION_SLACK_NONE = 0.1F;
    // 1^2 + 1^2 + 1^2 + (epsilon of 0.1F)
    public static final float DESTINATION_SLACK_ADJACENT = (float) Math.sqrt(2f);
    private static final double TIE_BREAKER = 1.001D;

    private CalculationNodeUtils() {
        throw new IllegalStateException("Can not instantiate an instance of: CalculationNodeUtils. This is a utility class");
    }

    public static boolean isADestination(@NotNull final CalculationNode n, final float destinationSlack, @NotNull final BlockPos destination) {
        if (destinationSlack <= DESTINATION_SLACK_NONE) {
            return n.pos.getX() == destination.getX()
                    && n.pos.getY() == destination.getY()
                    && n.pos.getZ() == destination.getZ();
        }

        if (n.pos.getY() == destination.getY() - 1) {
            return destination.closerThan(new Vec3i(n.pos.getX(), destination.getY(), n.pos.getZ()), destinationSlack);
        }

        return destination.closerThan(n.pos, destinationSlack);
    }
}
