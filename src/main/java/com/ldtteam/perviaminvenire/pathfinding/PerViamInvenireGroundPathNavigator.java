package com.ldtteam.perviaminvenire.pathfinding;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.ldtteam.perviaminvenire.api.adapters.registry.IDismountCartRegistry;
import com.ldtteam.perviaminvenire.api.adapters.registry.IRidingOnCartRegistry;
import com.ldtteam.perviaminvenire.api.adapters.registry.IRoadBlockRegistry;
import com.ldtteam.perviaminvenire.api.adapters.registry.ISpeedAdaptationRegistry;
import com.ldtteam.perviaminvenire.api.pathfinding.*;
import com.ldtteam.perviaminvenire.api.pathfinding.stuckhandling.CallbackBasedStuckHandler;
import com.ldtteam.perviaminvenire.api.pathfinding.stuckhandling.IStuckHandler;
import com.ldtteam.perviaminvenire.compat.vanilla.VanillaCompatibilityPath;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.util.Direction;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;
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
    public static final  double MIN_Y_DISTANCE           = 0.001;
    public static final  int    MAX_SPEED_ALLOWED        = 100;

    /**
     * Amount of ticks before vanilla stuck handling is allowed to discard an existing path
     */
    private static final long MIN_KEEP_TIME = 100;

    /**
     * The current result of the calculation
     */
    @Nullable
    private PathResult<AbstractPathJob> pathResult;

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
    public PerViamInvenireGroundPathNavigator(@NotNull final MobEntity entity)
    {
        this(entity, entity.getEntityWorld());
    }

    /**
     * Instantiates the navigation of an entity in a given world.
     *
     * @param entity the entity.
     * @param world  the world it is in.
     */
    public PerViamInvenireGroundPathNavigator(@NotNull final MobEntity entity, final World world)
    {
        super(entity, world);

        this.nodeProcessor = new WalkNodeProcessor();
        this.nodeProcessor.setCanEnterDoors(true);
        getPathingOptions().setEnterDoors(true);
        this.nodeProcessor.setCanOpenDoors(true);
        getPathingOptions().setCanOpenDoors(true);
        this.nodeProcessor.setCanSwim(true);
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
    public PathResult<AbstractPathJob> moveAwayFromXYZ(final BlockPos avoid, final double range, final double speed)
    {
        if (pathResult != null && !pathResult.isDone() && pathResult.getJob() instanceof PathJobMoveAwayFromLocation)
        {
            return pathResult;
        }

        return setPathJob(new PathJobMoveAwayFromLocation(ourEntity.getEntityWorld(),
          ourEntity.getPosition(),
          avoid,
          (int) range,
          (int) ourEntity.getAttribute(Attributes.FOLLOW_RANGE).getValue(),
          ourEntity), null, speed);
    }

    @Nullable
    public PathResult<AbstractPathJob> setPathJob(
      @NotNull final AbstractPathJob job,
      final BlockPos dest,
      final double speed)
    {
        clearPath();

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
            return this.additionalVanillaPathTasks.get(target, range);
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
        if (this.sourcePos != this.ourEntity.getPosition())
        {
            this.additionalVanillaPathTasks.values().forEach(VanillaCompatibilityPath::setCancelled);
            this.additionalVanillaPathTasks.clear();
            this.sourcePos = this.ourEntity.getPosition();
        }

        if (desiredPosTimeout > 0)
        {
            if (desiredPosTimeout-- <= 0)
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

        int oldIndex = this.noPath() ? 0 : Objects.requireNonNull(this.getPath()).getCurrentPathIndex();

        if (isSneaking)
        {
            isSneaking = false;
            entity.setSneaking(false);
        }
        this.ourEntity.setMoveVertical(0);
        if (handleLadders(oldIndex))
        {
            pathFollow();
            return;
        }
        if (handleRails())
        {
            return;
        }
        super.tick();

        if (pathResult != null && noPath())
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
    public PathResult moveToXYZ(final double x, final double y, final double z, final double speed)
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

        return setPathJob(
          new PathJobMoveToLocation(ourEntity.getEntityWorld(),
            ourEntity.getPosition(),
            target,
            (int) ourEntity.getAttribute(Attributes.FOLLOW_RANGE).getValue(),
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
    protected PathFinder getPathFinder(final int p_179679_1_)
    {
        return null;
    }

    @Override
    protected boolean canNavigate()
    {
        // Auto dismount when trying to path.
        @Nullable Entity lowestRidingEntity = ourEntity.getLowestRidingEntity();
        //noinspection ConstantConditions
        if (lowestRidingEntity != null && lowestRidingEntity != ourEntity)
        {
            @NotNull final PathPointExtended pEx = (PathPointExtended) Objects.requireNonNull(this.getPath()).getPathPointFromIndex(this.getPath().getCurrentPathIndex());
            return IDismountCartRegistry.getInstance()
                     .getRunner().handle(this.ourEntity, lowestRidingEntity, pEx)
                     .orElse(false);
        }
        return true;
    }

    @NotNull
    @Override
    protected Vector3d getEntityPosition()
    {
        return this.ourEntity.getPositionVec();
    }

    @Override
    public Path getPathToPos(@NotNull final BlockPos pos, final int range)
    {
        return scheduleAdditionalPath(
          pos,
          range,
          (blockPos, integer) -> new PathJobMoveToLocation(ourEntity.getEntityWorld(),
            ourEntity.getPosition(),
            blockPos,
            (int) ourEntity.getAttribute(Attributes.FOLLOW_RANGE).getValue(),
            ourEntity),
          Function.identity()
        );
    }

    @Override
    protected boolean isDirectPathBetweenPoints(final Vector3d start, @NotNull final Vector3d end, final int sizeX, final int sizeY, final int sizeZ)
    {
        return IRoadBlockRegistry.getInstance().getRunner().isRoad(ourEntity, world.getBlockState(new BlockPos(start.x, start.y - 1, start.z)).getBlock())
                 && super.isDirectPathBetweenPoints(start, end, sizeX, sizeY, sizeZ);
    }

    public double getSpeed()
    {
        speed = ISpeedAdaptationRegistry.getInstance().getRunner().get(ourEntity, requestedSpeed).orElse(walkSpeed);
        return speed;
    }

    @Override
    public void setSpeed(final double d)
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
    public boolean tryMoveToXYZ(final double x, final double y, final double z, final double speed)
    {
        if (x == 0 && y == 0 && z == 0)
        {
            return false;
        }

        moveToXYZ(x, y, z, speed);
        return true;
    }

    @Override
    public boolean tryMoveToEntityLiving(final Entity entityIn, final double speedIn)
    {
        return tryMoveToBlockPos(entityIn.getPosition(), speedIn);
    }

    // Removes stupid vanilla stuff, causing our pathpoints to occasionally be replaced by vanilla ones.
    @Override
    protected void trimPath() {}

    @Override
    public boolean setPath(@Nullable final Path path, final double speed)
    {
        if (path == null)
        {
            this.currentPath = null;
            return false;
        }
        pathStartTime = world.getGameTime();
        this.requestedSpeed = speed;
        this.desiredPos = path.target;
        return super.setPath(convertPath(path), speed);
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

        final int pathLength = path.getCurrentPathLength();
        Path tempPath = null;
        if (pathLength > 0 && !(path.getPathPointFromIndex(0) instanceof PathPointExtended))
        {
            //  Fix vanilla PathPoints to be PathPointExtended
            @NotNull final PathPointExtended[] newPoints = new PathPointExtended[pathLength];

            for (int i = 0; i < pathLength; ++i)
            {
                final PathPoint point = path.getPathPointFromIndex(i);
                if (!(point instanceof PathPointExtended))
                {
                    newPoints[i] = new PathPointExtended(new BlockPos(point.x, point.y, point.z));
                }
                else
                {
                    newPoints[i] = (PathPointExtended) point;
                }
            }

            tempPath = new Path(Arrays.asList(newPoints), getTargetPos(), false);

            final PathPointExtended finalPoint = newPoints[pathLength - 1];
            destination = new BlockPos(finalPoint.x, finalPoint.y, finalPoint.z);
        }

        return tempPath == null ? path : tempPath;
    }

    private boolean processCompletedCalculationResult()
    {
        setPath(pathResult.getPath(), getSpeed());
        pathResult.setStatus(PathFindingStatus.IN_PROGRESS_FOLLOWING);
        return false;
    }

    private boolean handleLadders(int oldIndex)
    {
        //  Ladder Workaround
        if (!this.noPath() && this.getPath() != null && this.getPath().getCurrentPathLength() > this.getPath().getCurrentPathIndex() + 1  && Objects.requireNonNull(this.getPath()).points.size() > this.getPath().getCurrentPathIndex())
        {
            @NotNull final PathPointExtended pEx = (PathPointExtended) Objects.requireNonNull(this.getPath()).getPathPointFromIndex(this.getPath().getCurrentPathIndex());
            final PathPointExtended pExNext = getPath().getCurrentPathLength() > this.getPath().getCurrentPathIndex() + 1
                                                ? (PathPointExtended) this.getPath()
                                                                        .getPathPointFromIndex(this.getPath()
                                                                                                 .getCurrentPathIndex() + 1)
                                                : null;

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
                if (IRoadBlockRegistry.getInstance().getRunner().isRoad(ourEntity, world.getBlockState(ourEntity.getPosition().down()).getBlock()))
                {
                    speed = ON_PATH_SPEED_MULTIPLIER * getSpeed();
                }
                else
                {
                    speed = getSpeed();
                }
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
        if (!this.noPath() && Objects.requireNonNull(this.getPath()).points.size() > this.getPath().getCurrentPathIndex())
        {
            @NotNull final PathPointExtended pEx = (PathPointExtended) Objects.requireNonNull(this.getPath()).getPathPointFromIndex(this.getPath().getCurrentPathIndex());
            final PathPointExtended pExNext = getPath().getCurrentPathLength() > this.getPath().getCurrentPathIndex() + 1
                                                ? (PathPointExtended) this.getPath()
                                                                        .getPathPointFromIndex(this.getPath()
                                                                                                 .getCurrentPathIndex() + 1)
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
    private boolean handlePathOnRails(final PathPointExtended pEx, final PathPointExtended pExNext)
    {
        return IRidingOnCartRegistry.getInstance().getRunner().handle(this.ourEntity, pEx, pExNext)
                 .orElseThrow(() -> new IllegalStateException(
                   "Entity : " + this.ourEntity.getType().getRegistryName() + " states that it can be used to ride on paths. But no handler for riding on carts is registered."));
    }

    private boolean handlePathPointOnLadder(final PathPointExtended pEx)
    {
        Vector3d vec3 = Objects.requireNonNull(this.getPath()).getPosition(this.ourEntity);

        if (vec3.squareDistanceTo(ourEntity.getPosX(), vec3.y, ourEntity.getPosZ()) < Math.random() * 0.1)
        {
            //This way he is less nervous and gets up the ladder
            double newSpeed = 0.05;
            switch (pEx.getLadderFacing())
            {
                //  Any of these values is climbing, so adjust our direction of travel towards the ladder
                case NORTH:
                    vec3 = vec3.add(0, 0, 1);
                    break;
                case SOUTH:
                    vec3 = vec3.add(0, 0, -1);
                    break;
                case WEST:
                    vec3 = vec3.add(1, 0, 0);
                    break;
                case EAST:
                    vec3 = vec3.add(-1, 0, 0);
                    break;
                case UP:
                    vec3 = vec3.add(0, 1, 0);
                    break;
                //  Any other value is going down, so lets not move at all
                default:
                    newSpeed = 0;
                    entity.setSneaking(true);
                    break;
            }

            if (newSpeed > 0)
            {
                this.ourEntity.getMoveHelper().setMoveTo(vec3.x, vec3.y, vec3.z, newSpeed);
            }
            else
            {
                if (world.getBlockState(ourEntity.getPosition().down()).isLadder(world, ourEntity.getPosition().down(), ourEntity))
                {
                    this.ourEntity.setMoveVertical(-0.5f);
                }
                else
                {
                    this.ourEntity.getNavigator().clearPath();
                }
                return true;
            }
        }
        return false;
    }

    private boolean handleEntityInWater(int oldIndex, final PathPointExtended pEx)
    {
        //  Prevent shortcuts when swimming
        final int curIndex = Objects.requireNonNull(this.getPath()).getCurrentPathIndex();
        if (curIndex > 0
              && (curIndex + 1) < this.getPath().getCurrentPathLength()
              && this.getPath().getPathPointFromIndex(curIndex - 1).y != pEx.y)
        {
            //  Work around the initial 'spin back' when dropping into water
            oldIndex = curIndex + 1;
        }

        this.getPath().setCurrentPathIndex(oldIndex);

        Vector3d vec3d = this.getPath().getPosition(this.ourEntity);

        if (vec3d.squareDistanceTo(new Vector3d(ourEntity.getPosX(), vec3d.y, ourEntity.getPosZ())) < 0.1
              && Math.abs(ourEntity.getPosY() - vec3d.y) < 0.5)
        {
            this.getPath().incrementPathIndex();
            if (this.noPath())
            {
                return true;
            }

            vec3d = this.getPath().getPosition(this.ourEntity);
        }

        ourEntity.setAIMoveSpeed((float) getSpeed());
        this.ourEntity.getMoveHelper().setMoveTo(vec3d.x, vec3d.y, vec3d.z, getSpeed());
        return false;
    }

    @Override
    protected void pathFollow()
    {
        getSpeed();
        final int curNode = Objects.requireNonNull(currentPath).getCurrentPathIndex();
        final int curNodeNext = curNode + 1;
        if (curNodeNext < currentPath.getCurrentPathLength())
        {
            if (!(currentPath.getPathPointFromIndex(curNode) instanceof PathPointExtended))
            {
                currentPath = convertPath(currentPath);
            }

            final PathPointExtended pEx = (PathPointExtended) currentPath.getPathPointFromIndex(curNode);
            final PathPointExtended pExNext = (PathPointExtended) currentPath.getPathPointFromIndex(curNodeNext);

            //  If current node is bottom of a ladder, then stay on this node until
            //  the ourEntity reaches the bottom, otherwise they will try to head out early
            if (pEx.isOnLadder() && pEx.getLadderFacing() == Direction.DOWN
                  && !pExNext.isOnLadder())
            {
                final Vector3d vec3 = getEntityPosition();
                if ((vec3.y - (double) pEx.y) < MIN_Y_DISTANCE)
                {
                    this.currentPath.setCurrentPathIndex(curNodeNext);
                }

                this.checkForStuck(vec3);
                return;
            }
        }

        if (currentPath.isFinished())
        {
            onPathFinish();
            return;
        }

        super.pathFollow();
    }

    /**
     * Called upon reaching the path end, reset values
     */
    public void onPathFinish()
    {
        clearPath();
    }

    public void updatePath() {}

    /**
     * Don't let vanilla rapidly discard paths, set a timeout before its allowed to use stuck.
     */
    @Override
    protected void checkForStuck(@NotNull final Vector3d positionVec3)
    {
        if (world.getGameTime() - pathStartTime < MIN_KEEP_TIME)
        {
            return;
        }

        if (this.totalTicks - this.ticksAtLastPos > 100)
        {
            if (positionVec3.squareDistanceTo(this.lastPosCheck) < 2.25D)
            {
                this.clearPath();
            }

            this.ticksAtLastPos = this.totalTicks;
            this.lastPosCheck = positionVec3;
        }

        if (this.currentPath != null && !this.currentPath.isFinished())
        {
            Vector3d vec3d = this.currentPath.getPosition(ourEntity);
            if (vec3d.equals(this.timeoutCachedNode))
            {
                this.timeoutTimer += Util.milliTime() - this.lastTimeoutCheck;
            }
            else
            {
                this.timeoutCachedNode = new Vector3i(vec3d.x, vec3d.y, vec3d.z);
                double d0 = positionVec3.distanceTo(vec3d);
                this.timeoutLimit = (this.entity.getAIMoveSpeed() > 0.0F ? d0 / (double) this.entity.getAIMoveSpeed() * 1000.0D : 0.0D) * 25;
            }

            if (this.timeoutLimit > 0.0D && (double) this.timeoutTimer > this.timeoutLimit * 3.0D)
            {
                this.timeoutCachedNode = Vector3i.NULL_VECTOR;
                this.timeoutTimer = 0L;
                this.timeoutLimit = 0.0D;
                this.clearPath();
            }

            this.lastTimeoutCheck = Util.milliTime();
        }
    }

    /**
     * If null path or reached the end.
     */
    @Override
    public boolean noPath()
    {
        return (pathResult == null || pathResult.isDone() && pathResult.getStatus() != PathFindingStatus.CALCULATION_COMPLETE) && super.noPath();
    }

    @Override
    public BlockPos getTargetPos()
    {
        if (!noPath())
        {
            if (pathResult != null)
                return pathResult.getPath().getTarget();
            else if (!super.noPath())
                return super.getTargetPos();
        }

        return BlockPos.ZERO;
    }

    @Nullable
    @Override
    public Path getPath()
    {
        if (!noPath())
        {
            if (pathResult != null)
                return pathResult.getPath();
            else if (!super.noPath())
                return super.getPath();
        }

        return null;
    }

    @Override
    public boolean getCanSwim()
    {
        return getPathingOptions().canSwim();
    }

    @Override
    public void clearPath()
    {

        if (pathResult != null)
        {
            pathResult.cancel();
            pathResult.setStatus(PathFindingStatus.CANCELLED);
            pathResult = null;
        }

        destination = null;
        super.clearPath();
    }

    /**
     * Used to move a living ourEntity with a speed.
     *
     * @param e     the ourEntity.
     * @param speed the speed.
     * @return the result.
     */
    @Nullable
    public PathResult moveToLivingEntity(@NotNull final Entity e, final double speed)
    {
        return moveToXYZ(e.getPosX(), e.getPosY(), e.getPosZ(), speed);
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
    public PathResult moveAwayFromLivingEntity(@NotNull final Entity e, final double distance, final double speed)
    {
        return moveAwayFromXYZ(e.getPosition(), distance, speed);
    }

    @Override
    public void setCanSwim(boolean canSwim)
    {
        super.setCanSwim(canSwim);
        getPathingOptions().setCanSwim(canSwim);
    }

    protected Path pathfind(Set<BlockPos> positions, int regionOffset, boolean offsetUpward, int distance)
    {
        if (positions.isEmpty())
        {
            return null;
        }
        else if (this.entity.getPosY() < 0.0D)
        {
            return null;
        }
        else if (!this.canNavigate())
        {
            return null;
        }

        if (currentPath instanceof VanillaCompatibilityPath)
        {
            final VanillaCompatibilityPath vanillaCompatibilityPath = (VanillaCompatibilityPath) currentPath;
            if (positions.contains(vanillaCompatibilityPath.getDestination()))
            {
                return currentPath;
            }
        }

        return scheduleAdditionalPath(
          positions,
          (int) ourEntity.getAttribute(Attributes.FOLLOW_RANGE).getValue() + regionOffset,
          (blockPos, integer) -> new PathJobMoveToOneOfLocation(ourEntity.getEntityWorld(),
            ourEntity.getPosition(),
            blockPos,
            (int) ourEntity.getAttribute(Attributes.FOLLOW_RANGE).getValue(),
            ourEntity),
          blockPos -> blockPos.stream().max(Comparator.comparing(ourEntity.getPosition()::distanceSq)).orElse(ourEntity.getPosition())
        );
    }
}
