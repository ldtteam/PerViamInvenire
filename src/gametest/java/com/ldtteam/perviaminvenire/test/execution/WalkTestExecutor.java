package com.ldtteam.perviaminvenire.test.execution;

import com.ldtteam.perviaminvenire.api.pathfinding.ICalculationResultTracker;
import com.ldtteam.perviaminvenire.test.template.TemplatePackManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

import java.util.function.Consumer;
import java.util.function.Function;

public final class WalkTestExecutor {

    private static final WalkTestExecutor INSTANCE = new WalkTestExecutor();

    public static WalkTestExecutor getInstance() {
        return INSTANCE;
    }

    private WalkTestExecutor() {
    }

    public <E extends Mob> Consumer<GameTestHelper> createSimpleWalkTestExecutionFor(final EntityType<E> entityType, int width) {
        return createWalkTestExecutionFor(entityType, width, (midPoint) -> new BlockPos(midPoint,2,midPoint), (midPoint) -> new BlockPos(TemplatePackManager.SIMPLE_WALK_PATH_LENGTH - 1,2,midPoint));
    }

    public <E extends Mob> Consumer<GameTestHelper> createJumpWalkTestExecutionFor(final EntityType<E> entityType, int width, final Direction direction, final int stepCount) {
        return createWalkTestExecutionFor(entityType, width, (midPoint) -> {
            final int startYOffset = direction == Direction.DOWN ? stepCount : 0;
            return new BlockPos(midPoint, 2 + startYOffset, midPoint);
        }, (midPoint) -> {
            final int endYOffset = direction == Direction.UP ? stepCount : 0;
            return new BlockPos(TemplatePackManager.SIMPLE_WALK_PATH_LENGTH - 1 + (width * stepCount), 2 + endYOffset, midPoint);
        });
    }

    public <E extends Mob> Consumer<GameTestHelper> createWalkTestExecutionFor(final EntityType<E> entityType, int width, final Function<Integer, BlockPos> startBuilder, final Function<Integer, BlockPos> targetBuilder) {
        return helper -> {
            int midPoint = (int) Math.ceil(width / 2f);
            if (width > 1) {
                midPoint++;
            }

            final BlockPos target = targetBuilder.apply(midPoint);
            final E entity = helper.spawnWithNoFreeWill(entityType, startBuilder.apply(midPoint));

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
