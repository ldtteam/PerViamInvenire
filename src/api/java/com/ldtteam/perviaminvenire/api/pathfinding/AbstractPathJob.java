package com.ldtteam.perviaminvenire.api.pathfinding;

import com.ldtteam.perviaminvenire.api.adapters.registry.IIsLadderBlockRegistry;
import com.ldtteam.perviaminvenire.api.adapters.registry.IRoadBlockRegistry;
import com.ldtteam.perviaminvenire.api.adapters.registry.IStartPositionAdapterRegistry;
import com.ldtteam.perviaminvenire.api.adapters.registry.IWalkableBlockRegistry;
import com.ldtteam.perviaminvenire.api.collisions.ICollisionDetectionManager;
import com.ldtteam.perviaminvenire.api.config.ICommonConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.Callable;

import static com.ldtteam.perviaminvenire.api.util.constants.PathingConstants.*;

/**
 * Abstract class for Jobs that run in the multi-threaded path finder.
 */
public abstract class AbstractPathJob implements Callable<Path>
{
    private static final Logger LOGGER = LogManager.getLogger();

    @NotNull
    protected final BlockPos                    start;
    @NotNull
    protected final LevelReader                world;
    protected final PathResult<? extends AbstractPathJob> result;
    private final   int                           maxRange;
    private final   Queue<CalculationNode>        nodesOpen    = new PriorityQueue<>(500);
    private final   Map<Integer, CalculationNode> nodesVisited = new HashMap<>();

    //  May be faster, but can produce strange results
    private final boolean allowJumpPointSearchTypeWalk;
    private final int invalidYLevel;
    private       int     totalNodesAdded   = 0;
    private       int     totalNodesVisited = 0;

    /**
     * Are there xz restrictions.
     */
    private final boolean xzRestricted;

    /**
     * Are xz restrictions hard or soft.
     */
    private final boolean hardXzRestriction;

    /**
     * The cost values for certain nodes.
     */
    private PathingOptions pathingOptions = new PathingOptions();

    /**
     * The restriction parameters
     */
    private int maxX;
    private int minX;
    private int maxZ;
    private int minZ;

    /**
     * The entity this job belongs to.
     */
    private final WeakReference<LivingEntity> entity;

    /**
     * The calculation data used to keep track of the calculation decisions made.
     */
    private final PathingCalculationData calculationData = new PathingCalculationData();

    /**
     * AbstractPathJob constructor.
     *
     * @param world  the world within which to path.
     * @param start  the start position from which to path from.
     * @param end    the end position to path to.
     * @param range  maximum path range.
     * @param entity the entity.
     */
    public AbstractPathJob(final Level world, @NotNull final BlockPos start, @NotNull final BlockPos end, final int range, final LivingEntity entity)
    {
        this(world, start, end, range, new PathResult<>(), entity);
    }

    /**
     * AbstractPathJob constructor.
     *
     * @param world  the world within which to path.
     * @param start  the start position from which to path from.
     * @param end    the end position to path to
     * @param range  maximum path range.
     * @param result path result.
     * @param entity the entity.
     * @see AbstractPathJob#AbstractPathJob(Level, BlockPos, BlockPos, int, LivingEntity)
     */
    public AbstractPathJob(
      final Level world,
      @NotNull final BlockPos start,
      @NotNull final BlockPos end,
      final int range,
      final PathResult<AbstractPathJob> result,
      final LivingEntity entity)
    {
        final int minX = Math.min(start.getX(), end.getX()) - (range / 2);
        final int minZ = Math.min(start.getZ(), end.getZ()) - (range / 2);
        final int maxX = Math.max(start.getX(), end.getX()) + (range / 2);
        final int maxZ = Math.max(start.getZ(), end.getZ()) + (range / 2);

        this.invalidYLevel = world.getMinBuildHeight() - 1;
        this.xzRestricted = false;
        this.hardXzRestriction = false;

        this.world = new ChunkCache(world, new BlockPos(minX, MIN_Y, minZ), new BlockPos(maxX, MAX_Y, maxZ), range);

        this.maxRange = range;

        this.result = result;
        result.setJob(this);

        allowJumpPointSearchTypeWalk = false;
        this.entity = new WeakReference<>(entity);

        //This needs to be last since this is what potentially invokes the start handling.
        this.start = new BlockPos(this.prepareStart(entity, entity.blockPosition()));

    }

    /**
     * AbstractPathJob constructor.
     *
     * @param world            the world within which to path.
     * @param start            the start position from which to path from.
     * @param startRestriction start of restricted area.
     * @param endRestriction   end of restricted area.
     * @param range            range^2 is used as cap for visited node count
     * @param hardRestriction  if <code>true</code> start has to be inside the restricted area (otherwise the search immediately finishes) -
     *                         node visits outside the area are not allowed, isAtDestination is called on every node, if <code>false</code>
     *                         restricted area only applies to calling isAtDestination thus searching outside area is allowed
     * @param result           path result.
     * @param entity           the entity.
     */
    public AbstractPathJob(final Level world,
        final BlockPos start,
        final BlockPos startRestriction,
        final BlockPos endRestriction,
        final int range,
        final boolean hardRestriction,
        final PathResult<AbstractPathJob> result,
        final LivingEntity entity)
    {
        this(world, start, startRestriction, endRestriction, range, Vec3i.ZERO, hardRestriction, result, entity);
    }

    /**
     * AbstractPathJob constructor.
     *
     * @param world            the world within which to path.
     * @param start            the start position from which to path from.
     * @param startRestriction start of restricted area.
     * @param endRestriction   end of restricted area.
     * @param range            range^2 is used as cap for visited node count
     * @param grow             adjustment for restricted area, can be either shrink or grow, is applied in both of xz directions after
     *                         getting min/max box values
     * @param hardRestriction  if <code>true</code> start has to be inside the restricted area (otherwise the search immediately finishes) -
     *                         node visits outside the area are not allowed, isAtDestination is called on every node, if <code>false</code>
     *                         restricted area only applies to calling isAtDestination thus searching outside area is allowed
     * @param result           path result.
     * @param entity           the entity.
     */
    public AbstractPathJob(final Level world,
        @NotNull final BlockPos start,
        final BlockPos startRestriction,
        final BlockPos endRestriction,
        final int range,
        final Vec3i grow,
        final boolean hardRestriction,
        final PathResult<AbstractPathJob> result,
        final LivingEntity entity)
    {
        this.minX = Math.min(startRestriction.getX(), endRestriction.getX()) - grow.getX();
        this.minZ = Math.min(startRestriction.getZ(), endRestriction.getZ()) - grow.getZ();
        this.maxX = Math.max(startRestriction.getX(), endRestriction.getX()) + grow.getX();
        this.maxZ = Math.max(startRestriction.getZ(), endRestriction.getZ()) + grow.getZ();

        this.invalidYLevel = world.getMinBuildHeight() - 1;
        this.xzRestricted = true;
        this.hardXzRestriction = hardRestriction;

        this.world = new ChunkCache(world, new BlockPos(minX, MIN_Y, minZ), new BlockPos(maxX, MAX_Y, maxZ), range);

        this.maxRange = range;

        this.result = result;
        result.setJob(this);

        this.allowJumpPointSearchTypeWalk = false;
        this.entity = new WeakReference<>(entity);

        this.start = this.prepareStart(entity, start);
    }

    /**
     * Indicates if the given node is a ladder going up.
     *
     * @param currentNode the current node.
     * @param dPos the delta position.
     * @return true if so.
     */
    private static boolean onLadderGoingUp(@NotNull final CalculationNode currentNode, @NotNull final BlockPos dPos)
    {
        return currentNode.isLadder() && (dPos.getY() >= 0 || dPos.getX() != 0 || dPos.getZ() != 0);
    }

    /**
     * Generates a good path starting location for the entity to path from, correcting for the following conditions. - Being in water: pathfinding in water occurs along the
     * surface; adjusts position to surface. - Being in a fence space: finds correct adjacent position which is not a fence space, to prevent starting path. from within the fence
     * block.
     *
     * @param entity Entity for the pathfinding operation.
     * @return ChunkCoordinates for starting location.
     */
    public BlockPos prepareStart(@NotNull final LivingEntity entity, final BlockPos start)
    {
        return IStartPositionAdapterRegistry.getInstance().getRunner().apply(this, entity, start).orElse(entity.blockPosition());
    }

    /**
     * Sets the direction where the ladder is facing.
     *
     * @param world the world in.
     * @param pos   the position.
     * @param p     the path.
     */
    private static void setLadderFacing(@NotNull final LevelReader world, final BlockPos pos, @NotNull final ExtendedNode p)
    {
        final BlockState state = world.getBlockState(pos);
        final Block block = state.getBlock();
        if (block instanceof VineBlock)
        {
            if (state.getValue(VineBlock.SOUTH))
            {
                p.setLadderFacing(Direction.NORTH);
            }
            else if (state.getValue(VineBlock.WEST))
            {
                p.setLadderFacing(Direction.EAST);
            }
            else if (state.getValue(VineBlock.NORTH))
            {
                p.setLadderFacing(Direction.SOUTH);
            }
            else if (state.getValue(VineBlock.EAST))
            {
                p.setLadderFacing(Direction.WEST);
            }
        }
        else if (block instanceof LadderBlock)
        {
            p.setLadderFacing(state.getValue(LadderBlock.FACING));
        }
        else if (block instanceof ScaffoldingBlock)
        {
            p.setLadderFacing(Direction.UP);
        }
        else
        {
            p.setLadderFacing(Direction.UP);
        }
    }

    /**
     * Checks if entity is on a ladder.
     *
     * @param node       the path node.
     * @param nextInPath the next path point.
     * @param pos        the position.
     * @return true if on a ladder.
     */
    private static boolean onALadder(@NotNull final CalculationNode node, @Nullable final CalculationNode nextInPath, @NotNull final BlockPos pos)
    {
        return nextInPath != null && node.isLadder()
                 &&
                 (nextInPath.pos.getX() == pos.getX() && nextInPath.pos.getZ() == pos.getZ());
    }

    /**
     * Generate a pseudo-unique key for identifying a given node by it's coordinates Encodes the lowest 12 bits of x,z and all useful bits of y. This creates unique keys for all
     * blocks within a 4096x256x4096 cube, which is FAR bigger volume than one should attempt to path find within This version takes a BlockPos
     *
     * @param pos BlockPos to generate key from
     * @return key for node in map
     */
    private static int computeNodeKey(@NotNull final BlockPos pos)
    {
        return ((pos.getX() & 0xFFF) << SHIFT_X_BY)
                 | ((pos.getY() & 0xFF) << SHIFT_Y_BY)
                 | (pos.getZ() & 0xFFF);
    }

    /**
     * Compute the cost (immediate 'g' value) of moving from the parent space to the new space.
     *
     * @param dPos       The delta from the parent to the new space; assumes dx,dy,dz in range of [-1..1].
     * @param isSwimming true is the current node would require the citizen to swim.
     * @param isLadder   checks if the node is on a ladder.
     * @param onPath     checks if the node is on a path.
     * @param onRails    checks if the node is a rail block.
     * @param railsExit  the exit of the rails.
     * @param blockPos   the position.
     * @param swimStart  if its the swim start.
     * @return cost to move from the parent to the new position.
     */
    protected double computeCost(
      @NotNull final BlockPos dPos,
      final boolean isSwimming,
      final boolean isLadder,
      final boolean onPath,
      final boolean onRails,
      final boolean railsExit,
      final boolean swimStart,
      final BlockPos blockPos)
    {
        double cost = Math.sqrt(dPos.getX() * dPos.getX() + dPos.getY() * dPos.getY() + dPos.getZ() * dPos.getZ());

        if (dPos.getY() != 0 && (dPos.getX() != 0 || dPos.getZ() != 0) && !(Math.abs(dPos.getY()) <= 1 && world.getBlockState(blockPos).getBlock() instanceof StairBlock))
        {
            //  Tax the cost for jumping, dropping
            cost *= pathingOptions.jumpDropCost() * Math.abs(dPos.getY());
        }

        if (world.getBlockState(blockPos).hasProperty(BlockStateProperties.OPEN))
        {
            cost *= pathingOptions.traverseToggleAbleCost();
        }

        if (onPath)
        {
            cost *= pathingOptions.onPathCost();
        }

        if (onRails)
        {
            cost *= pathingOptions.onRailCost();
        }

        if (railsExit)
        {
            cost *= pathingOptions.railsExitCost();
        }

        if (isLadder)
        {
            cost *= pathingOptions.onLadderCost();
        }

        if (isSwimming)
        {
            if (swimStart)
            {
                cost *= pathingOptions.swimCostEnter();
            }
            else
            {
                cost *= pathingOptions.swimCost();
            }
        }

        return cost;
    }

    /**
     * Indicates if the node is closed.
     *
     * @param node the node.
     * @return true if so.
     */
    private static boolean nodeClosed(@Nullable final CalculationNode node)
    {
        return node != null && node.isClosed();
    }

    /**
     * Indicates if the node is a swimming node.
     *
     * @param world the world.
     * @param pos the position.
     * @param node the node.
     * @return true if so.
     */
    private static boolean calculateSwimming(@NotNull final LevelReader world, @NotNull final BlockPos pos, @Nullable final CalculationNode node)
    {
        return (node == null) ? isWater(world, pos.below()) : node.isSwimming();
    }

    /**
     * Check if the block at this position is actually some kind of waterly fluid.
     * @param pos the pos in the world.
     * @return true if so.
     */
    private static boolean isWater(@NotNull final LevelReader world, final BlockPos pos)
    {
        final BlockState state = world.getBlockState(pos);
        if (state.canOcclude())
        {
            return false;
        }
        if (state.getBlock() == Blocks.WATER)
        {
            return true;
        }

        final FluidState fluidState = world.getFluidState(pos);
        if (fluidState.isEmpty())
        {
            return false;
        }

        final Fluid fluid = fluidState.getType();
        return fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER;
    }

    /**
     * Gives access to the calculated path result.
     *
     * @return the result.
     */
    public PathResult<? extends AbstractPathJob> getResult()
    {
        return result;
    }

    /**
     * Callable method for initiating asynchronous task.
     *
     * @return path to follow or null.
     */
    @Override
    public final Path call()
    {
        try
        {
            return search();
        }
        catch (final Exception e)
        {
            // Log everything, so exceptions of the pathfinding-thread show in Log
            LOGGER.warn("Pathfinding Exception", e);
        }

        return null;
    }

    /**
     * Perform the search.
     *
     * @return Path of a path to the given location, a best-effort, or null.
     */
    @Nullable
    protected Path search()
    {
        CalculationNode bestNode = getAndSetupStartNode();

        double bestNodeResultScore = Double.MAX_VALUE;

        while (!nodesOpen.isEmpty())
        {
            if (Thread.currentThread().isInterrupted())
            {
                return null;
            }

            final CalculationNode currentNode = nodesOpen.poll();

            totalNodesVisited++;

            // Limiting max amount of nodes mapped
            if (totalNodesVisited > ICommonConfig.getInstance().getMaxPathFindingNodes() || totalNodesVisited > maxRange * maxRange)
            {
                break;
            }
            currentNode.setCounterVisited(totalNodesVisited);

            currentNode.setClosed();

            calculationData.onNodeConsumed(currentNode.pos);

            final boolean isPositionOk = !xzRestricted || (currentNode.pos.getX() >= minX && currentNode.pos.getX() <= maxX
                && currentNode.pos.getZ() >= minZ && currentNode.pos.getZ() <= maxZ);

            // if xz restricted then disallow check destination outside the restricted area
            if (isPositionOk && isAtDestination(currentNode))
            {
                bestNode = currentNode;
                result.setPathReachesDestination(true);
                break;
            }

            // If this is the closest node to our destination, treat it as our best node
            final double nodeResultScore = getNodeResultScore(currentNode);
            if (isPositionOk && nodeResultScore < bestNodeResultScore && !currentNode.isCornerNode()
                  && isWalkableSurface(world.getBlockState(currentNode.pos.below()), currentNode.pos.below()) == SurfaceType.WALKABLE)
            {
                bestNode = currentNode;
                bestNodeResultScore = nodeResultScore;
            }

            // if xz soft-restricted we can walk outside the restricted area to be able to find ways around back to the area
            if (!hardXzRestriction || isPositionOk)
            {
                walkCurrentNode(currentNode);
            }
        }

        @NotNull final Path path = finalizePath(bestNode);

        final Entity entity;
        if ((entity = this.entity.get()) != null)
        {
            calculationData.onPathCompleted(path);
            if (entity.getCommandSenderWorld() instanceof final ServerLevel world)
            {
                world.getServer().execute(() -> IPathingResultHandler.getInstance().onCompleted(
                  calculationData,
                  this.entity.get(),
                  world
                ));
            }
        }

        ICalculationResultTracker.getInstance().onCalculationCompleted(this);
        return path;
    }

    private void walkCurrentNode(@NotNull final CalculationNode currentNode)
    {
        BlockPos dPos = BLOCKPOS_IDENTITY;
        if (currentNode.parent != null)
        {
            dPos = currentNode.pos.subtract(currentNode.parent.pos);
        }

        //  On a ladder, we can go 1 straight-up
        if (onLadderGoingUp(currentNode, dPos) && pathingOptions.canUseLadders())
        {
            walk(currentNode, BLOCKPOS_UP);
        }

        // Walk upwards node if swimming and floating
        if (!isNotPassable(currentNode.pos, currentNode.pos.above()) && currentNode.isSwimming() && isLiquid(world.getBlockState(currentNode.pos.above())) && pathingOptions.canFloat())
        {
            walk(currentNode, BLOCKPOS_UP);
            //Since we can swim upwards, we need to check that.
            //But we do not need to abort because we can float.
            //In contrast to the if switch for checking below a couple lines after this, which needs to abort if we can't float.
        }

        //  We can also go down 1, if the lower block is a ladder
        if (onLadderGoingDown(currentNode, dPos) && pathingOptions.canUseLadders())
        {
            walk(currentNode, BLOCKPOS_DOWN);
        }

        // Only explore downwards when dropping
        if ((currentNode.parent == null || !currentNode.parent.pos.equals(currentNode.pos.below())) && currentNode.isCornerNode())
        {
            walk(currentNode, BLOCKPOS_DOWN);
            return;
        }

        // Walk downwards node if passable
        if (!isNotPassable(currentNode.pos, currentNode.pos.below()) && (!currentNode.isSwimming() || (currentNode.isSwimming() && isLiquid(world.getBlockState(currentNode.pos.below())))))
        {
            walk(currentNode, BLOCKPOS_DOWN);

            if (currentNode.isSwimming() && isLiquid(world.getBlockState(currentNode.pos.below())) && !pathingOptions.canFloat())
            {
                // We searched downwards because we are in a fluid we can swim in. But we can not float.
                // so we are not proceeding horizontally.
                // This would not trigger if the block below our feed is not a fluid, because we would not have searched downwards.
                return;
            }
        }

        // N
        if (dPos.getZ() <= 0)
        {
            walk(currentNode, BLOCKPOS_NORTH);
        }

        // E
        if (dPos.getX() >= 0)
        {
            walk(currentNode, BLOCKPOS_EAST);
        }

        // S
        if (dPos.getZ() >= 0)
        {
            walk(currentNode, BLOCKPOS_SOUTH);
        }

        // W
        if (dPos.getX() <= 0)
        {
            walk(currentNode, BLOCKPOS_WEST);
        }
    }

    /**
     * Check if this is a liquid state for swimming.
     * @param state the state to check.
     * @return true if so.
     */
    public boolean isLiquid(final BlockState state)
    {
        return state.getMaterial().isLiquid() || (!state.getMaterial().blocksMotion() && !state.getFluidState().isEmpty());
    }

    private boolean onLadderGoingDown(@NotNull final CalculationNode currentNode, @NotNull final BlockPos dPos)
    {
        return (dPos.getY() <= 0 || dPos.getX() != 0 || dPos.getZ() != 0) && isLadder(currentNode.pos.below());
    }

    @NotNull
    private CalculationNode getAndSetupStartNode()
    {
        @NotNull final CalculationNode startNode = new CalculationNode(start,
                world.getBlockState(start), computeHeuristic(start));

        if (isLadder(start))
        {
            startNode.setLadder();
        }
        else if (isLiquid(world.getBlockState(start.below())) || (getEntity() instanceof WaterAnimal && isLiquid(world.getBlockState(start))))
        {
            startNode.setSwimming();
        }

        startNode.setOnRails(pathingOptions.canUseRails() && world.getBlockState(start).getBlock() instanceof BaseRailBlock);

        offerNode(startNode, startNode);
        nodesVisited.put(computeNodeKey(start), startNode);

        ++totalNodesAdded;

        return startNode;
    }

    private void offerNode(final CalculationNode source, final CalculationNode node)
    {
        nodesOpen.offer(node);
        calculationData.onNodeWalked(source.pos, node.pos);
    }

    /**
     * Generate the path to the target node.
     *
     * @param targetNode the node to path to.
     * @return the path.
     */
    @NotNull
    private Path finalizePath(@NotNull final CalculationNode targetNode)
    {
        Validate.notNull(targetNode);
        //  Compute length of path, since we need to allocate an array.  This is cheaper/faster than building a List
        //  and converting it.  Yes, we have targetNode.steps, but I do not want to rely on that being accurate (I might
        //  fudge that value later on for cutoff purposes
        int pathLength = 0;
        int railsLength = 0;
        CalculationNode node = targetNode;
        while (node.parent != null)
        {
            ++pathLength;
            if (node.isOnRails())
            {
                ++railsLength;
            }
            node = node.parent;
        }

        @NotNull final Node[] points = new Node[pathLength];

        @Nullable CalculationNode nextInPath = null;
        @Nullable ExtendedNode next = null;
        node = targetNode;
        while (node.parent != null)
        {
            --pathLength;

            @NotNull final BlockPos pos = node.pos;

            if (node.isSwimming())
            {
                //  Not truly necessary but helps prevent them spinning in place at swimming nodes
                pos.offset(BLOCKPOS_DOWN);
            }

            @NotNull final ExtendedNode p = new ExtendedNode(pos);
            if (railsLength >= ICommonConfig.getInstance().getMinimumRailsToUseInPath())
            {
                p.setOnRails(node.isOnRails());
                if (p.isOnRails() && (!node.parent.isOnRails() || node.parent.parent == null))
                {
                    p.setRailsEntry();
                }
                else if (p.isOnRails() && points.length > pathLength + 1)
                {
                    final ExtendedNode point = ((ExtendedNode) points[pathLength + 1]);
                    if (!point.isOnRails())
                    {
                        point.setRailsExit();
                    }
                }
            }

            //  Climbing on a ladder?
            if (onALadder(node, nextInPath, pos))
            {
                p.setOnLadder(true);
                if (nextInPath.pos.getY() >= pos.getY())
                {
                    //  We only care about facing if going up
                    //In the case of BlockVines (Which does not have Direction) we have to check the metadata of the vines... bitwise...
                    setLadderFacing(world, pos, p);
                }
            }
            else if (onALadder(node.parent, node.parent, pos))
            {
                p.setOnLadder(true);
                p.setLadderFacing(Direction.UP);
            }

            if (next != null)
            {
                next.cameFrom = p;
            }
            next = p;
            points[pathLength] = p;

            nextInPath = node;
            node = node.parent;
        }

        return new Path(Arrays.asList(points), getNodePosition(targetNode), isAtDestination(targetNode));
    }

    /**
     * Gets the position of a given node.
     *
     * @param node The node.
     * @return THe position.
     */
    protected BlockPos getNodePosition(final CalculationNode node)
    {
        return node.pos;
    }

    /**
     * Compute the heuristic cost ('h' value) of a given position x,y,z.
     * <p>
     * Returning a value of 0 performs a breadth-first search. Returning a value less than actual possible cost to goal guarantees shortest path, but at computational expense.
     * Returning a value exactly equal to the cost to the goal guarantees shortest path and least expense (but generally. only works when path is straight and unblocked). Returning
     * a value greater than the actual cost to goal produces good, but not perfect paths, and is fast. Returning a very high value (such that 'h' is very high relative to 'g') then
     * only 'h' (the heuristic) matters as the search will be a very fast greedy best-first-search, ignoring cost weighting and distance.
     *
     * @param pos Position to compute heuristic from.
     * @return the heuristic.
     */
    protected abstract double computeHeuristic(BlockPos pos);

    /**
     * Return true if the given node is a viable final destination, and the path should generate to here.
     *
     * @param n Node to test.
     * @return true if the node is a viable destination.
     */
    protected abstract boolean isAtDestination(CalculationNode n);

    /**
     * Compute a 'result score' for the Node; if no destination is determined, the node that had the highest 'result' score is used.
     *
     * @param n Node to test.
     * @return score for the node.
     */
    protected abstract double getNodeResultScore(CalculationNode n);

    /**
     * "Walk" from the parent in the direction specified by the delta, determining the new x,y,z position for such a move and adding or updating a node, as appropriate.
     *
     * @param parent Node being walked from.
     * @param dPos   Delta from parent, expected in range of [-1..1].
     */
    protected final void walk(@NotNull final CalculationNode parent, @NotNull BlockPos dPos)
    {
        BlockPos pos = parent.pos.offset(dPos);

        //  Can we traverse into this node?  Fix the y up
        final int newY = getGroundHeight(parent, pos);

        if (newY < world.getMinBuildHeight())
        {
            return;
        }

        boolean corner = false;
        if (pos.getY() != newY)
        {
            if (parent.isCornerNode() && (dPos.getX() != 0 || dPos.getZ() != 0))
            {
                return;
            }

            // if the new position is above the current node, we're taking the node directly above
            if (!parent.isCornerNode() && newY - parent.pos.getY() > 0 && (parent.parent == null || !parent.parent.pos.equals(parent.pos.offset(new BlockPos(0,
              newY - pos.getY(),
              0)))))
            {
                dPos = new BlockPos(0, newY - pos.getY(), 0);
                pos = parent.pos.offset(dPos);
                corner = true;
            }
            // If we're going down, take the air-corner before going to the lower node
            else if (!parent.isCornerNode() && newY - parent.pos.getY() < 0 && (dPos.getX() != 0 || dPos.getZ() != 0) && (parent.parent == null || !parent.pos.below()
                                                                                                                                               .equals(parent.parent.pos)))
            {
                dPos = new BlockPos(dPos.getX(), 0, dPos.getZ());
                pos = parent.pos.offset(dPos);
                corner = true;
            }
            // Fix up normal y
            else
            {
                dPos = dPos.offset(0, newY - pos.getY(), 0);
                pos = new BlockPos(pos.getX(), newY, pos.getZ());
            }
        }

        int nodeKey = computeNodeKey(pos);
        CalculationNode node = nodesVisited.get(nodeKey);
        if (nodeClosed(node))
        {
            //  Early out on closed nodes (closed = expanded from)
            return;
        }

        final boolean isSwimming = calculateSwimming(world, pos, node);

        if (isSwimming && !pathingOptions.canSwim())
        {
            calculationData.onInvalidNode(pos, PathingCalculationData.InvalidNodeReason.SWIMMING_NODE);
            return;
        }

        final boolean swimStart = isSwimming && !parent.isSwimming();
        final boolean onLadder = isLadder(pos);
        final boolean onRoad = IRoadBlockRegistry.getInstance().getRunner().isRoad(this.entity.get(), world.getBlockState(pos.below()).getBlock());
        final boolean onRails = pathingOptions.canUseRails() && world.getBlockState(corner ? pos.below() : pos).getBlock() instanceof BaseRailBlock;
        final boolean railsExit = !onRails && parent.isOnRails();

        //  Cost may have changed due to a jump up or drop
        final double stepCost = computeCost(dPos, isSwimming, onLadder, onRoad, onRails, railsExit, swimStart, pos);
        final double heuristic = computeHeuristic(pos);
        final double cost = parent.getCost() + stepCost;
        final double score = cost + heuristic;

        if (node == null)
        {
            node = createNode(parent, pos, nodeKey, isSwimming, heuristic, cost, score);
            node.setOnRails(onRails);
            node.setCornerNode(corner);
            if (parent.isLadder() && node.isCornerNode())
            {
                node.setLadder();
            }
        }
        else if (updateCurrentNode(parent, node, heuristic, cost, score))
        {
            return;
        }

        offerNode(parent, node);

        //  Jump Point Search-ish optimization:
        // If this node was a (heuristic-based) improvement on our parent,
        // lets go another step in the same direction...
        performJumpPointSearch(parent, dPos, node);
    }

    private void performJumpPointSearch(@NotNull final CalculationNode parent, @NotNull final BlockPos dPos, @NotNull final CalculationNode node)
    {
        if (allowJumpPointSearchTypeWalk && node.getHeuristic() <= parent.getHeuristic())
        {
            walk(node, dPos);
        }
    }

    @NotNull
    private CalculationNode createNode(
      final CalculationNode parent, @NotNull final BlockPos pos, final int nodeKey,
      final boolean isSwimming, final double heuristic, final double cost, final double score)
    {
        final CalculationNode node;
        node = new CalculationNode(parent, pos, world.getBlockState(pos), cost, heuristic, score);
        nodesVisited.put(nodeKey, node);

        if (isLadder(pos))
        {
            node.setLadder();
        }
        else if (isSwimming)
        {
            node.setSwimming();
        }

        totalNodesAdded++;
        node.setCounterAdded(totalNodesAdded);
        return node;
    }

    private boolean updateCurrentNode(@NotNull final CalculationNode parent, @NotNull final CalculationNode node, final double heuristic, final double cost, final double score)
    {
        //  This node already exists
        if (score >= node.getScore())
        {
            return true;
        }

        if (!nodesOpen.remove(node))
        {
            return true;
        }

        node.parent = parent;
        node.setSteps(parent.getSteps() + 1);
        node.setCost(cost);
        node.setHeuristic(heuristic);
        node.setScore(score);
        return false;
    }

    /**
     * Get the height of the ground at the given x,z coordinate, within 1 step of y.
     *
     * @param parent parent node.
     * @param pos    coordinate of block.
     * @return y height of first open, viable block above ground, or {@link AbstractPathJob#invalidYLevel} if blocked or too far a drop.
     */
    public int getGroundHeight(@Nullable final CalculationNode parent, @NotNull final BlockPos pos)
    {
        final Entity entity;
        if ((entity = this.entity.get()) == null)
            return invalidYLevel;

        final Vec3 facing = Vec3.atLowerCornerOf(pos.subtract(parent == null ? pos : parent.pos));

        if (!ICollisionDetectionManager.getInstance().canFit(
          entity,
          pos,
          facing,
          this.world
        ))
        {
            return handleTargetNotPassable(parent, pos);
        }

        //  Do we have something to stand on in the target space?
        final BlockState below = world.getBlockState(pos.below());
        final SurfaceType surfaceType = isWalkableSurface(below, pos);
        if (surfaceType == SurfaceType.WALKABLE)
        {
            //  Level path
            return pos.getY();
        }
        else if (surfaceType == SurfaceType.NOT_PASSABLE)
        {
            return invalidYLevel;
        }

        return handleNotStanding(parent, pos, below);
    }

    private int handleNotStanding(@Nullable final CalculationNode parent, @NotNull final BlockPos pos, @NotNull final BlockState below)
    {
        final boolean isSwimming = parent != null && parent.isSwimming();

        if (below.getMaterial().isLiquid())
        {
            return handleInLiquid(pos, below, isSwimming);
        }

        if (isLadder(below, pos.below()))
        {
            return pos.getY();
        }

        return checkDrop(parent, pos, isSwimming);
    }

    private int checkDrop(@Nullable final CalculationNode parent, @NotNull final BlockPos pos, final boolean isSwimming)
    {
        final boolean canDrop = parent != null && !parent.isLadder();
        //  Nothing to stand on
        if (!canDrop || isSwimming || ((parent.pos.getX() != pos.getX() || parent.pos.getZ() != pos.getZ()) && !isNotPassable(parent.pos, parent.pos.below())
                                         && isWalkableSurface(world.getBlockState(parent.pos.below()), parent.pos.below()) == SurfaceType.DROPABLE))
        {
            return invalidYLevel;
        }

        for (int i = 2; i <= 10; i++)
        {
            final BlockState below = world.getBlockState(pos.below(i));
            if (isWalkableSurface(below, pos) == SurfaceType.WALKABLE && i <= 4 || below.getMaterial().isLiquid())
            {
                //  Level path
                return pos.getY() - i + 1;
            }
            else if (below.getMaterial() != Material.AIR)
            {
                return invalidYLevel;
            }
        }

        return invalidYLevel;
    }

    private int handleInLiquid(@NotNull final BlockPos pos, @NotNull final BlockState below, final boolean isSwimming)
    {
        if (isSwimming)
        {
            //  Already swimming in something, or allowed to swim and this is water
            return pos.getY();
        }

        if (pathingOptions.canSwim() && below.getMaterial() == Material.WATER)
        {
            if (pathingOptions.canFloat())
                //  This is water, and we are allowed to swim
                return pos.getY();

            return pos.getY() - 1;
        }

        //  Not allowed to swim or this isn't water, and we're on dry land
        return invalidYLevel;
    }

    private int handleTargetNotPassable(@Nullable final CalculationNode parent, @NotNull final BlockPos pos)
    {
        final boolean canJump = parent != null && !parent.isLadder() && !parent.isSwimming();
        //  Need to try jumping up one, if we can
        if (!canJump)
        {
            return invalidYLevel;
        }

        //  Check for jump room from the origin space
        if (isNotPassable(parent.pos, parent.pos.above()))
        {
            return invalidYLevel;
        }

        return !isNotPassable(parent.pos.above(), pos.above()) ? pos.above().getY() : invalidYLevel;
    }

    protected boolean isNotPassable(final BlockPos parent, final BlockPos pos)
    {
        final Entity entity;
        if ((entity = this.entity.get()) == null)
            return false;

        return !ICollisionDetectionManager.getInstance().canFit(
          entity,
          pos,
          Vec3.atLowerCornerOf(pos.subtract(parent)),
          this.world
        );
    }

    /**
     * Is the block solid and can be stood upon.
     *
     * @param blockState Block to check.
     * @param pos        the position.
     * @return true if the block at that location can be walked on.
     */
    @NotNull
    protected SurfaceType isWalkableSurface(@NotNull final BlockState blockState, final BlockPos pos)
    {
        return IWalkableBlockRegistry.getInstance().getRunner().get(this.pathingOptions, this.entity.get(), blockState, pos).orElseGet(() -> {
            final Block block = blockState.getBlock();
            if (block instanceof FenceBlock
                  || block instanceof FenceGateBlock
                  || block instanceof WallBlock
                  || block instanceof FireBlock
                  || block instanceof CampfireBlock
                  || block instanceof BambooBlock
                  || (blockState.getShape(world, pos).max(Direction.Axis.Y) > 1.0))
            {
                return SurfaceType.NOT_PASSABLE;
            }

            final FluidState fluid = world.getFluidState(pos);
            if (blockState.getBlock() == Blocks.LAVA || (!fluid.isEmpty() && (fluid.getType() == Fluids.LAVA || fluid.getType() == Fluids.FLOWING_LAVA)))
            {
                return SurfaceType.NOT_PASSABLE;
            }

            if (blockState.getMaterial().isSolid() || (blockState.getBlock() == Blocks.SNOW && blockState.getValue(SnowLayerBlock.LAYERS) > 1)
                  || blockState.getBlock() instanceof WoolCarpetBlock)
            {
                return SurfaceType.WALKABLE;
            }

            return SurfaceType.DROPABLE;
        });
    }

    /**
     * Is the blockstate a ladder.
     *
     * @param blockstate blockstate to check.
     * @param pos   location of the blockstate.
     * @return true if the blockstate is a ladder.
     */
    protected boolean isLadder(@NotNull final BlockState blockstate, final BlockPos pos)
    {
        return IIsLadderBlockRegistry.getInstance()
          .getRunner().isLadder(this.getEntity(), blockstate, world, pos)
            .orElseGet(() -> blockstate.getBlock().isLadder(this.world.getBlockState(pos), world, pos, entity.get()));
    }

    protected boolean isLadder(final BlockPos pos)
    {
        return isLadder(world.getBlockState(pos), pos);
    }

    /**
     * Sets the pathing options
     *
     * @param pathingOptions the pathing options to set.
     */
    public void setPathingOptions(final PathingOptions pathingOptions)
    {
        this.pathingOptions = pathingOptions;
    }

    @NotNull
    public PathingCalculationData getCalculationData()
    {
        return calculationData;
    }

    @Nullable
    public LivingEntity getEntity()
    {
        return this.entity.get();
    }
}
