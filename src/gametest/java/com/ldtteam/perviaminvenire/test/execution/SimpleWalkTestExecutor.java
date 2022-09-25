package com.ldtteam.perviaminvenire.test.execution;

import com.ldtteam.perviaminvenire.api.pathfinding.ICalculationResultTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

import java.util.function.Consumer;

public final class SimpleWalkTestExecutor {

    private static final SimpleWalkTestExecutor INSTANCE = new SimpleWalkTestExecutor();

    public static SimpleWalkTestExecutor getInstance() {
        return INSTANCE;
    }

    private SimpleWalkTestExecutor() {
    }

    public <E extends Mob> Consumer<GameTestHelper> createSimpleWalkTestExecutionFor(final EntityType<E> entityType, int width) {
        return helper -> {
            int midPoint = (int) Math.ceil(width / 2f);
            if (width > 1) {
                midPoint++;
            }

            final BlockPos target = new BlockPos(10,2,midPoint);
            final E entity = helper.spawnWithNoFreeWill(entityType, new BlockPos(1,2,midPoint));

            helper.startSequence().thenExecute(() -> helper.getLevel().getServer().getPlayerList().getPlayers().forEach(player -> {
                ICalculationResultTracker.getInstance().startTracking(player, entity);
            })).thenExecuteAfter(2, () -> {
                final BlockPos absoluteTarget = helper.absolutePos(target);
                entity.getNavigation().moveTo(absoluteTarget.getX(), absoluteTarget.getY(), absoluteTarget.getZ(), 1);
            }).thenWaitUntil(() -> helper.assertEntityPresent(entityType, target, 1)).thenExecute(() -> helper.getLevel().getServer().getPlayerList().getPlayers().forEach(player -> {
                ICalculationResultTracker.getInstance().stopTracking(player, entity);
            })).thenSucceed();
        };
    }
}
