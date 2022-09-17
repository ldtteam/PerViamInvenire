package com.ldtteam.perviaminvenire.pathfinding;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.ldtteam.perviaminvenire.api.adapters.registry.*;
import com.ldtteam.perviaminvenire.api.pathfinding.*;
import com.ldtteam.perviaminvenire.api.pathfinding.stuckhandling.CallbackBasedStuckHandler;
import com.ldtteam.perviaminvenire.api.pathfinding.stuckhandling.IStuckHandler;
import com.ldtteam.perviaminvenire.compat.vanilla.VanillaCompatibilityPath;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * PVI async PathNavigator.
 */
public class PerViamInvenireGroundPathNavigator extends AbstractAdvancedGroundPathNavigator
{
    private static final Logger LOGGER = LogManager.getLogger(PerViamInvenireGroundPathNavigator.class);

    private static final double ON_PATH_SPEED_MULTIPLIER = 1.3D;
    public static final  int    MAX_SPEED_ALLOWED        = 100;

    /**
     * Amount of ticks before vanilla stuck handling is allowed to discard an existing path
     */
    private static final long MIN_KEEP_TIME = 100;

    /**
     * The current result of the calculation
     */
    @Nullable
    private PathResult<? extends AbstractPathJob> pathResult;

    /**
     * These are additional tasks that are currently being run in case vanilla asks for path finding data.
     */
    @NotNull
    private final Table<Object, Integer, VanillaCompatibilityPath> additionalVanillaPathTasks = HashBasedTable.create();

    /**
     * The last position the entity for this navigator was on. If this changes path calculation are cancelled.
     */
    @NotNull
    private BlockPos sourcePos = BlockPos.ZERO;

    /**
     * The world time when a path was added.
     */
    private long pathStartTime = 0;

    /**
     * Desired position to reach
     */
    private BlockPos desiredPos;

    /**
     * Timeout for the desired pos, resets when its no longer wanted
     */
    private int desiredPosTimeout = 0;

    /**
     * The stuck handler to use
     */
    private IStuckHandler stuckHandler;

    /**
     * Whether we did set sneaking
     */
    private boolean isSneaking = true;

    /**
     * Instantiates the navigation of an entity in its world.
     *
     * @param entity The entity.
     */
    @SuppressWarnings("unused")
    public PerViamInvenireGroundPathNavigator(@NotNull final Mob entity)
    {
        this(entity, entity.getCommandSenderWorld());
    }

    /**
     * Instantiates the navigation of an entity in a given world.
     *
     * @param entity the entity.
     * @param world  the world it is in.
     */
    public PerViamInvenireGroundPathNavigator(@NotNull final Mob entity, final Level world)
    {
        super(entity, world);

        this.nodeEvaluator = new WalkNodeEvaluator();
        this.nodeEvaluator.setCanPassDoors(true);
        getPathingOptions().setEnterDoors(true);
        this.nodeEvaluator.setCanOpenDoors(true);
        getPathingOptions().setCanOpenDoors(true);
        this.nodeEvaluator.setCanFloat(true);
        getPathingOptions().setCanSwim(true);

        stuckHandler = CallbackBasedStuckHandler.create().withCanBeStuckCallback(() -> true).withTakeDamageOnStuck(0.2f).withTeleportSteps(6).withTeleportOnFullStuck();
    }

    /**
     * Get the destination from the path.
     *
     * @return the destination position.
     */
    public BlockPos getDestination()
    {
        return destination;
    }

    /**
     * Used to path away from a position.
     *
     * @param avoid the position to avoid.
     * @param range the range he should move out of.
     * @param speed the speed to run at.
     * @return the result of the pathing.
     */
    @Nullable
    public PathResult<? extends AbstractPathJob> moveAwayFromXYZ(final BlockPos avoid, final double range, final double speed)
    {
        if (pathResult != null && !pathResult.isDone() && pathResult.getJob() instanceof PathJobMoveAwayFromLocation)
        {
            return pathResult;
        }

        final AttributeInstance followRangeAttribute;
        if ((followRangeAttribute = ourEntity.getAttribute(Attributes.FOLLOW_RANGE)) == null)
        {
            return null;
        }

        return setPathJob(new PathJobMoveAwayFromLocation(ourEntity.getCommandSenderWorld(),
          ourEntity.blockPosition(),
          avoid,
          (int) range,
          (int) followRangeAttribute.getValue(),
          ourEntity), null, speed);
    }

    @Nullable
    public PathResult<? extends AbstractPathJob> setPathJob(
      @NotNull final AbstractPathJob job,
      final BlockPos dest,
      final double speed)
    {
        stopCurrentCalculation();

        this.destination = dest;
        this.originalDestination = dest;
        if (dest != null)
        {
            desiredPos = dest;
            desiredPosTimeout = 50 * 20;
        }
        this.walkSpeed = speed;

        if (speed > MAX_SPEED_ALLOWED)
        {
            LOGGER.error("Tried to set a too high speed for entity:" + ourEntity, new Exception());
            return null;
        }

        job.setPathingOptions(getPathingOptions());
        pathResult = job.getResult();
        pathResult.startJob(PathFinding.getExecutor());
        return pathResult;
    }

    @Nullable
    public <T> VanillaCompatibilityPath scheduleAdditionalPath(
      @NotNull final T target,
      final int range,
      final BiFunction<T, Integer, AbstractPathJob> jobBuilder,
      final Function<T, BlockPos> altTargetBuilder
    )
    {
        if (this.additionalVanillaPathTasks.contains(target, range))
        {
            final VanillaCompatibilityPath cachedPath = this.additionalVanillaPathTasks.get(target, range);

            // Same logic vanilla does for results
            if (cachedPath == null || cachedPath.isCalculationComplete() && cachedPath.getNodeCount() < 2)
            {
                this.additionalVanillaPathTasks.remove(target, range);
                return null;
            }
            return cachedPath;
        }

        final AbstractPathJob job = jobBuilder.apply(target, range);
        job.setPathingOptions(getPathingOptions());

        final Future<Path> future = PathFinding.enqueue(job);
        final VanillaCompatibilityPath path = new VanillaCompatibilityPath(
          sourcePos,
          altTargetBuilder.apply(target),
          future
        );
        this.additionalVanillaPathTasks.put(target, range, path);

        return path;
    }

    @Override
    public void tick()
    {
        if (this.sourcePos != this.ourEntity.blockPosition())
        {
            this.additionalVanillaPathTasks.values().forEach(VanillaCompatibilityPath::setCancelled);
            this.additionalVanillaPathTasks.clear();
            this.sourcePos = this.ourEntity.blockPosition();
        }

        if (desiredPosTimeout > 0)
        {
            desiredPosTimeout--;
            if (desiredPosTimeout <= 0)
            {
                desiredPos = null;
            }
        }

        if (pathResult != null)
        {
            if (!pathResult.isDone())
            {
                return;
            }
            else if (pathResult.getStatus() == PathFindingStatus.CALCULATION_COMPLETE)
            {
                processCompletedCalculationResult();
            }
        }

        int oldIndex = this.isDone() ? 0 : Objects.requireNonNull(this.getPath()).getNextNodeIndex();

        if (isSneaking)
        {
            isSneaking = false;
            mob.setShiftKeyDown(false);
        }
        this.ourEntity.setYya(0);
        if (handleLadders(oldIndex))
        {
            followThePath();
            return;
        }
        if (handleRails())
        {
            return;
        }
        super.tick();

        if (pathResult != null && isDone())
        {
            pathResult.setStatus(PathFindingStatus.COMPLETE);
            pathResult = null;
        }

        stuckHandler.checkStuck(this);
    }

    /**
     * Try to move to a certain position.
     *
     * @param x     the x target.
     * @param y     the y target.
     * @param z     the z target.
     * @param speed the speed to walk.
     * @return the PathResult.
     */
    @Nullable
    public PathResult<? extends AbstractPathJob> moveToXYZ(final double x, final double y, final double z, final double speed)
    {
        final BlockPos target = new BlockPos(x, y, z);

        if (pathResult != null && pathResult.getJob() instanceof PathJobMoveToLocation &&
              (
                pathResult.isComputing()
                  || (destination != null && destination.equals(target))
                  || (originalDestination != null && originalDestination.equals(target))
              )
        )
        {
            return pathResult;
        }

        final AttributeInstance followRangeAttribute;
        if ((followRangeAttribute = ourEntity.getAttribute(Attributes.FOLLOW_RANGE)) == null)
        {
            return null;
        }

        return setPathJob(
          new PathJobMoveToLocation(ourEntity.getCommandSenderWorld(),
            ourEntity.blockPosition(),
            target,
            (int) followRangeAttribute.getValue(),
            ourEntity),
          target, speed);
    }

    public boolean tryMoveToBlockPos(final BlockPos pos, final double speed)
    {
        moveToXYZ(pos.getX(), pos.getY(), pos.getZ(), speed);
        return true;
    }

    @SuppressWarnings("ConstantConditions")
    @NotNull
    @Override
    protected PathFinder createPathFinder(final int p_179679_1_)
    {
        return new PathFinder(null, p_179679_1_);
    }

    @Override
    protected boolean canUpdatePath()
    {
        // Auto dismount when trying to path.
        @Nullable Entity lowestRidingEntity = ourEntity.getRootVehicle();
        //noinspection ConstantConditions
        if (this.getPath() != null && lowestRidingEntity != null && lowestRidingEntity != ourEntity)
        {
            @NotNull final ExtendedNode pEx = (ExtendedNode) Objects.requireNonNull(this.getPath()).getNode(this.getPath().getNextNodeIndex());
            return IDismountCartRegistry.getInstance()
                     .getRunner().handle(this.ourEntity, lowestRidingEntity, pEx)
                     .orElse(false);
        }
        return true;
    }

    @NotNull
    @Override
    protected Vec3 getTempMobPos()
    {
        return this.ourEntity.position();
    }

    @Override
    public Path createPath(@NotNull final BlockPos pos, final int range)
    {
        final AttributeInstance followRangeAttribute;
        if ((followRangeAttribute = ourEntity.getAttribute(Attributes.FOLLOW_RANGE)) == null)
        {
            return null;
        }

        return scheduleAdditionalPath(
          pos,
          range,
          (blockPos, integer) -> new PathJobMoveToLocation(ourEntity.getCommandSenderWorld(),
            ourEntity.blockPosition(),
            blockPos,
            (int) followRangeAttribute.getValue(),
            ourEntity),
          Function.identity()
        );
    }

    @Override
    protected boolean canMoveDirectly(@NotNull final Vec3 start, @NotNull final Vec3 end) {
        return IRoadBlockRegistry.getInstance().getRunner().isRoad(ourEntity, level.getBlockState(new BlockPos(start.x, start.y - 1, start.z)).getBlock())
             && super.canMoveDirectly(start, end);
    }

    public double getSpeed()
    {
        speedModifier = ISpeedAdaptationRegistry.getInstance().getRunner().get(ourEntity, requestedSpeed).orElse(walkSpeed);
        return speedModifier;
    }

    @Override
    public void setSpeedModifier(final double d)
    {
        if (d > MAX_SPEED_ALLOWED)
        {
            LOGGER.error("Tried to set a too high speed for entity:" + ourEntity, new Exception());
            return;
        }
        walkSpeed = d;
    }

    /**
     * Deprecated - try to use BlockPos instead
     */
    @Override
    public boolean moveTo(final double x, final double y, final double z, final double speed)
    {
        if (x == 0 && y == 0 && z == 0)
        {
            return false;
        }

        moveToXYZ(x, y, z, speed);
        return true;
    }

    @Override
    public boolean moveTo(final Entity entityIn, final double speedIn)
    {
        return tryMoveToBlockPos(entityIn.blockPosition(), speedIn);
    }

    // Removes stupid vanilla stuff, causing our pathpoints to occasionally be replaced by vanilla ones.
    @Override
    protected void trimPath() {}

    @Override
    public boolean moveTo(@Nullable final Path path, final double speed)
    {
        if (path == null)
        {
            this.path = null;
            return false;
        }
        pathStartTime = level.getGameTime();
        this.requestedSpeed = speed;
        this.desiredPos = path.target;
        return super.moveTo(convertPath(path), speed);
    }

    /**
     * Converts the given path to a minecolonies path if needed.
     *
     * @param path given path
     * @return resulting path
     */
    private Path convertPath(final Path path)
    {
        if (path instanceof VanillaCompatibilityPath)
        {
            return path;
        }

        final int pathLength = path.getNodeCount();
        Path tempPath = null;
        if (pathLength > 0 && !(path.getNode(0) instanceof ExtendedNode))
        {
            //  Fix vanilla PathPoints to be PathPointExtended
            @NotNull final ExtendedNode[] newPoints = new ExtendedNode[pathLength];

            for (int i = 0; i < pathLength; ++i)
            {
                final Node point = path.getNode(i);
                if (!(point instanceof ExtendedNode))
                {
                    newPoints[i] = new ExtendedNode(new BlockPos(point.x, point.y, point.z));
                }
                else
                {
                    newPoints[i] = (ExtendedNode) point;
                }
            }

            tempPath = new Path(Arrays.asList(newPoints), getTargetPos(), false);

            final ExtendedNode finalPoint = newPoints[pathLength - 1];
            destination = new BlockPos(finalPoint.x, finalPoint.y, finalPoint.z);
        }

        return tempPath == null ? path : tempPath;
    }

    private void processCompletedCalculationResult()
    {
        if (pathResult == null)
        {
            return;
        }

        moveTo(pathResult.getPath(), getSpeed());
        pathResult.setStatus(PathFindingStatus.IN_PROGRESS_FOLLOWING);
    }

    private boolean handleLadders(int oldIndex)
    {
        //  Ladder Workaround
        if (!this.isDone() && this.getPath() != null && this.getPath().getNodeCount() > this.getPath().getNextNodeIndex() + 1
              && Objects.requireNonNull(this.getPath()).nodes.size() > this.getPath().getNextNodeIndex())
        {
            @NotNull final ExtendedNode pEx = (ExtendedNode) Objects.requireNonNull(this.getPath()).getNode(this.getPath().getNextNodeIndex());
            final ExtendedNode pExNext = getPath().getNodeCount() > this.getPath().getNextNodeIndex() + 1
                                                ? (ExtendedNode) this.getPath()
                                                                        .getNode(this.getPath()
                                                                                                 .getNextNodeIndex() + 1)
                                                : null;

            for (int i = this.getPath().getNextNodeIndex(); i < Math.min(this.getPath().getNodeCount(), this.getPath().getNextNodeIndex() + 3); i++)
            {
                final ExtendedNode nextPoints = (ExtendedNode) this.getPath().getNode(i);
                if (nextPoints.isOnLadder())
                {
                    Vec3 motion = this.mob.getDeltaMovement();
                    double x = motion.x < -0.1 ? -0.1 : Math.min(motion.x, 0.1);
                    double z = motion.x < -0.1 ? -0.1 : Math.min(motion.z, 0.1);

                    this.ourEntity.setDeltaMovement(x, motion.y, z);
                    break;
                }
            }

            if (getPathingOptions().canUseLadders() && pEx.isOnLadder() && (pExNext != null && pEx.y != pExNext.y))
            {
                return handlePathPointOnLadder(pEx);
            }
            else if (ourEntity.isInWater())
            {
                return handleEntityInWater(oldIndex, pEx);
            }
            else
            {
                if (IRoadBlockRegistry.getInstance().getRunner().isRoad(ourEntity, level.getBlockState(ourEntity.blockPosition().below()).getBlock()))
                {
                    speedModifier = ON_PATH_SPEED_MULTIPLIER * getSpeed();
                }
                else
                {
                    speedModifier = getSpeed();
                }
            }

            if (pEx.isOnLadder() && pExNext != null && !pExNext.isOnLadder())
            {
                // Ladder exit motion bump
                Vec3 motion = this.mob.getDeltaMovement();
                double xMotion = motion.x;
                if (pEx.x > pExNext.x)
                {
                    xMotion -= 0.2;
                }

                if (pExNext.x > pEx.x)
                {
                    xMotion += 0.2;
                }

                double zMotion = motion.z;
                if (pEx.z > pExNext.z)
                {
                    zMotion -= 0.2;
                }

                if (pExNext.z > pEx.z)
                {
                    zMotion += 0.2;
                }

                this.ourEntity.setDeltaMovement(xMotion, motion.y + 0.2, zMotion);
            }
        }
        return false;
    }

    /**
     * Handle rails navigation.
     *
     * @return true if block.
     */
    private boolean handleRails()
    {
        if (!this.isDone() && Objects.requireNonNull(this.getPath()).nodes.size() > this.getPath().getNextNodeIndex())
        {
            @NotNull final ExtendedNode pEx = (ExtendedNode) Objects.requireNonNull(this.getPath()).getNode(this.getPath().getNextNodeIndex());
            final ExtendedNode pExNext = getPath().getNodeCount() > this.getPath().getNextNodeIndex() + 1
                                                ? (ExtendedNode) this.getPath()
                                                                        .getNode(this.getPath()
                                                                                                 .getNextNodeIndex() + 1)
                                                : null;

            if (pEx.isOnRails() || pEx.isRailsExit())
            {
                return handlePathOnRails(pEx, pExNext);
            }
        }
        return false;
    }

    /**
     * Handle pathing on rails.
     *
     * @param pEx     the current path point.
     * @param pExNext the next path point.
     * @return if go to next point.
     */
    private boolean handlePathOnRails(final ExtendedNode pEx, final ExtendedNode pExNext)
    {
        return IRidingOnCartRegistry.getInstance().getRunner().handle(this.ourEntity, pEx, pExNext)
                 .orElseThrow(() -> new IllegalStateException(
                   "Entity : " + ForgeRegistries.ENTITY_TYPES.getKey(getOurEntity().getType()) + " states that it can be used to ride on paths. But no handler for riding on carts is registered."));
    }

    private boolean handlePathPointOnLadder(final ExtendedNode pEx)
    {
        Vec3 vec3 = Objects.requireNonNull(this.getPath()).getNextEntityPos(this.ourEntity);

        if (vec3.distanceToSqr(ourEntity.getX(), vec3.y, ourEntity.getZ()) < (this.ourEntity.getBbWidth() * 0.5)
              && Math.abs(vec3.y() - ourEntity.getY()) < 1.5)
        {
            //This way he is less nervous and gets up the ladder
            double newSpeed = 0.05;
            switch (pEx.getLadderFacing())
            {
                //  Any of these values is climbing, so adjust our direction of travel towards the ladder
                case NORTH -> vec3 = vec3.add(0, 0, 1);
                case SOUTH -> vec3 = vec3.add(0, 0, -1);
                case WEST -> vec3 = vec3.add(1, 0, 0);
                case EAST -> vec3 = vec3.add(-1, 0, 0);
                case UP -> vec3 = vec3.add(0, 1, 0);

                //  Any other value is going down, so lets not move at all
                default -> {
                    newSpeed = 0;
                    mob.setShiftKeyDown(true);
                }
            }

            if (newSpeed > 0)
            {
                if (!(level.getBlockState(ourEntity.blockPosition()).getBlock() instanceof LadderBlock))
                {
                    this.ourEntity.setDeltaMovement(this.ourEntity.getDeltaMovement().add(0, 0.1D, 0));
                }
                this.ourEntity.getMoveControl().setWantedPosition(vec3.x, vec3.y, vec3.z, newSpeed);
            }
            else
            {
                if (isLadder(level.getBlockState(ourEntity.blockPosition().below()), ourEntity.blockPosition().below()))
                {
                    this.ourEntity.setYya(-0.5f);
                }
                else
                {
                    this.ourEntity.getNavigation().stop();
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Is the blockstate a ladder.
     *
     * @param blockstate blockstate to check.
     * @param pos        location of the blockstate.
     * @return true if the blockstate is a ladder.
     */
    protected boolean isLadder(@NotNull final BlockState blockstate, final BlockPos pos)
    {
        return IIsLadderBlockRegistry.getInstance()
                 .getRunner().isLadder(this.mob, blockstate, level, pos)
                 .orElseGet(() -> blockstate.getBlock().isLadder(this.level.getBlockState(pos), level, pos, mob));
    }

    private boolean handleEntityInWater(int oldIndex, final ExtendedNode pEx)
    {
        //  Prevent shortcuts when swimming
        final int curIndex = Objects.requireNonNull(this.getPath()).getNextNodeIndex();
        if (curIndex > 0
              && (curIndex + 1) < this.getPath().getNodeCount()
              && this.getPath().getNode(curIndex - 1).y != pEx.y)
        {
            //  Work around the initial 'spin back' when dropping into water
            oldIndex = curIndex + 1;
        }

        this.getPath().setNextNodeIndex(oldIndex);

        Vec3 vec3d = this.getPath().getNextEntityPos(this.ourEntity);

        if (vec3d.distanceToSqr(new Vec3(ourEntity.getX(), vec3d.y, ourEntity.getZ())) < 0.1
              && Math.abs(ourEntity.getY() - vec3d.y) < 0.5)
        {
            this.getPath().advance();
            if (this.isDone())
            {
                return true;
            }

            vec3d = this.getPath().getNextEntityPos(this.ourEntity);
        }

        ourEntity.setSpeed((float) getSpeed());
        this.ourEntity.getMoveControl().setWantedPosition(vec3d.x, vec3d.y, vec3d.z, getSpeed());
        return false;
    }

    @Override
    protected void followThePath()
    {
        Path currentPathToFollow = getPath();
        if (currentPathToFollow == null)
            return;

        getSpeed();
        final int curNode = currentPathToFollow.getNextNodeIndex();
        final int curNodeNext = curNode + 1;
        if (curNodeNext < currentPathToFollow.getNodeCount())
        {
            if (!(currentPathToFollow.getNode(curNode) instanceof ExtendedNode))
            {
                path = convertPath(getPath());
                currentPathToFollow = getPath();
            }

            this.maxDistanceToWaypoint = this.mob.getBbWidth() > 0.75F ? this.mob.getBbWidth() / 2.0F : 0.75F - this.mob.getBbWidth() / 2.0F;
        }

        if (currentPathToFollow.isDone())
        {
            onPathFinish();
            return;
        }

        currentPathToFollow = getPath();
        if (currentPathToFollow == null)
            return;

        this.maxDistanceToWaypoint = this.mob.getBbWidth() > 0.75F ? this.mob.getBbWidth() / 2.0F : 0.75F - this.mob.getBbWidth() / 2.0F;
        boolean wentAhead = false;


        // Look at multiple points, incase we're too fast
        for (int i = currentPathToFollow.getNextNodeIndex(); i < Math.min(currentPathToFollow.getNodeCount(), currentPathToFollow.getNextNodeIndex() + 4); i++)
        {
            Vec3 next = currentPathToFollow.getEntityPosAtNode(this.mob, i);
            if (Math.abs(this.ourEntity.getX() - next.x) < (double) this.maxDistanceToWaypoint - Math.abs(this.ourEntity.getY() - (next.y)) * 0.1
                  && Math.abs(this.ourEntity.getZ() - next.z) < (double) this.maxDistanceToWaypoint - Math.abs(this.ourEntity.getY() - (next.y)) * 0.1 &&
                  Math.abs(this.ourEntity.getY() - next.y) < 1.0D)
            {
                currentPathToFollow.advance();
                wentAhead = true;
            }
        }

        if (currentPathToFollow.isDone())
        {
            onPathFinish();
            return;
        }

        if (wentAhead)
        {
            return;
        }

        if (curNode >= currentPathToFollow.getNodeCount() || curNode <= 1)
        {
            return;
        }

        // Check some past nodes case we fell behind.
        final Vec3 curr = currentPathToFollow.getEntityPosAtNode(this.mob, curNode - 1);
        final Vec3 next = currentPathToFollow.getEntityPosAtNode(this.mob, curNode);

        if (mob.position().distanceTo(curr) >= 2.0 && mob.position().distanceTo(next) >= 2.0)
        {
            int currentIndex = curNode - 1;
            while (currentIndex > 0)
            {
                final Vec3 tempoPos = currentPathToFollow.getEntityPosAtNode(this.mob, currentIndex);
                if (mob.position().distanceTo(tempoPos) <= 1.0)
                {
                    currentPathToFollow.setNextNodeIndex(currentIndex);
                }
                currentIndex--;
            }
        }
    }

    /**
     * Called upon reaching the path end, reset values
     */
    public void onPathFinish()
    {
        stopCurrentCalculation();
    }

    public void recomputePath() {}

    /**
     * Don't let vanilla rapidly discard paths, set a timeout before its allowed to use stuck.
     */
    @Override
    protected void doStuckDetection(@NotNull final Vec3 positionVec3)
    {
        if (level.getGameTime() - pathStartTime < MIN_KEEP_TIME)
        {
            return;
        }

        if (this.tick - this.lastStuckCheck > 100)
        {
            if (positionVec3.distanceToSqr(this.lastStuckCheckPos) < 2.25D)
            {
                this.stopCurrentCalculation();
            }

            this.lastStuckCheck = this.tick;
            this.lastStuckCheckPos = positionVec3;
        }

        if (this.getPath() != null && !this.getPath().isDone())
        {
            Vec3 vec3d = this.getPath().getNextEntityPos(ourEntity);
            if (new BlockPos(vec3d).equals(this.timeoutCachedNode))
            {
                this.timeoutTimer += Util.getMillis() - this.lastTimeoutCheck;
            }
            else
            {
                this.timeoutCachedNode = new Vec3i(vec3d.x, vec3d.y, vec3d.z);
                double d0 = positionVec3.distanceTo(vec3d);
                this.timeoutLimit = (this.mob.getSpeed() > 0.0F ? d0 / (double) this.mob.getSpeed() * 1000.0D : 0.0D) * 25;
            }

            if (this.timeoutLimit > 0.0D && (double) this.timeoutTimer > this.timeoutLimit * 3.0D)
            {
                this.timeoutCachedNode = Vec3i.ZERO;
                this.timeoutTimer = 0L;
                this.timeoutLimit = 0.0D;
                this.stopCurrentCalculation();
            }

            this.lastTimeoutCheck = Util.getMillis();
        }
    }

    /**
     * If null path or reached the end.
     */
    @Override
    public boolean isDone()
    {
        return (pathResult == null || pathResult.isDone() && pathResult.getStatus() != PathFindingStatus.CALCULATION_COMPLETE) && super.isDone();
    }

    @NotNull
    @Override
    public BlockPos getTargetPos()
    {
        if (!isDone())
        {
            if (pathResult != null && pathResult.getPath() != null)
            {
                return pathResult.getPath().getTarget();
            }
            else if (!super.isDone())
            {
                final BlockPos pos = super.getTargetPos();
                return pos == null ? BlockPos.ZERO : pos;
            }
        }

        return BlockPos.ZERO;
    }

    @Nullable
    @Override
    public Path getPath()
    {
        if (!isDone())
        {
            if (pathResult != null)
            {
                return pathResult.getPath();
            }
            else if (!super.isDone())
            {
                return super.getPath();
            }
        }

        return null;
    }

    @Override
    public boolean canFloat()
    {
        return getPathingOptions().canSwim();
    }

    @Override
    public void stop()
    {
        if (pathResult != null)
        {
            pathResult.cancel();
            pathResult.setStatus(PathFindingStatus.CANCELLED);
            pathResult = null;
        }

        destination = null;
        super.stop();
    }

    @Override
    public void stopCurrentCalculation()
    {
        this.stop();
    }

    /**
     * Used to move a living ourEntity with a speed.
     *
     * @param e     the ourEntity.
     * @param speed the speed.
     * @return the result.
     */
    @Nullable
    public PathResult<? extends AbstractPathJob> moveToLivingEntity(@NotNull final Entity e, final double speed)
    {
        return moveToXYZ(e.getX(), e.getY(), e.getZ(), speed);
    }

    @Override
    public BlockPos getDesiredPos()
    {
        return this.desiredPos;
    }

    @Override
    public void setStuckHandler(final IStuckHandler stuckHandler)
    {
        this.stuckHandler = stuckHandler;
    }

    /**
     * Used to path away from a ourEntity.
     *
     * @param e        the ourEntity.
     * @param distance the distance to move to.
     * @param speed    the speed to run at.
     * @return the result of the pathing.
     */
    @Nullable
    public PathResult<? extends AbstractPathJob> moveAwayFromLivingEntity(@NotNull final Entity e, final double distance, final double speed)
    {
        return moveAwayFromXYZ(e.blockPosition(), distance, speed);
    }

    @Override
    public void setCanFloat(boolean canSwim)
    {
        super.setCanFloat(canSwim);
        getPathingOptions().setCanSwim(canSwim);
    }

    protected Path createPath(Set<BlockPos> positions, int regionOffset, boolean offsetUpward, int distance)
    {
        if (positions.isEmpty())
        {
            return null;
        }
        else if (this.mob.getY() < 0.0D)
        {
            return null;
        }
        else if (!this.canUpdatePath())
        {
            return null;
        }

        if (path instanceof final VanillaCompatibilityPath vanillaCompatibilityPath)
        {
            if (positions.contains(vanillaCompatibilityPath.getDestination()))
            {
                return path;
            }
        }

        final AttributeInstance followRangeAttribute;
        if ((followRangeAttribute = ourEntity.getAttribute(Attributes.FOLLOW_RANGE)) == null)
        {
            return null;
        }


        return scheduleAdditionalPath(
          positions,
          (int) followRangeAttribute.getValue() + regionOffset,
          (blockPos, integer) -> new PathJobMoveToOneOfLocation(ourEntity.getCommandSenderWorld(),
            ourEntity.blockPosition(),
            blockPos,
            (int) followRangeAttribute.getValue(),
            ourEntity),
          blockPos -> blockPos.stream().max(Comparator.comparing(ourEntity.blockPosition()::distSqr)).orElse(ourEntity.blockPosition())
        );
    }
}
