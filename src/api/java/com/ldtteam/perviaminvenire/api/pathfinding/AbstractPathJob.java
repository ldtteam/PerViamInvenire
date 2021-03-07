package com.ldtteam.perviaminvenire.api.pathfinding;

import com.ldtteam.perviaminvenire.api.adapters.registry.IPassableBlockRegistry;
import com.ldtteam.perviaminvenire.api.adapters.registry.IRoadBlockRegistry;
import com.ldtteam.perviaminvenire.api.adapters.registry.IStartPositionAdapterRegistry;
import com.ldtteam.perviaminvenire.api.adapters.registry.IWalkableBlockRegistry;
import com.ldtteam.perviaminvenire.api.config.ICommonConfig;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.Half;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
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
 * Abstract class for Jobs that run in the multithreaded path finder.
 */
public abstract class AbstractPathJob implements Callable<Path>
{
    private static final Logger LOGGER = LogManager.getLogger();

    @NotNull
    protected final BlockPos                    start;
    @NotNull
    protected final IWorldReader                world;
    protected final PathResult<AbstractPathJob> result;
    private final   int                         maxRange;
    private final   Queue<Node>                 nodesOpen    = new PriorityQueue<>(500);
    private final   Map<Integer, Node>          nodesVisited = new HashMap<>();

    //  May be faster, but can produce strange results
    private final boolean allowJumpPointSearchTypeWalk;
    private       int     totalNodesAdded   = 0;
    private       int     totalNodesVisited = 0;

    /**
     * Are there hard xz restrictions.
     */
    private boolean xzRestricted = false;

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
    public AbstractPathJob(final World world, @NotNull final BlockPos start, @NotNull final BlockPos end, final int range, final LivingEntity entity)
    {
        this(world, start, end, range, new PathResult<AbstractPathJob>(), entity);
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
     * @see AbstractPathJob#AbstractPathJob(World, BlockPos, BlockPos, int, LivingEntity)
     */
    public AbstractPathJob(
      final World world,
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

        this.world = new ChunkCache(world, new BlockPos(minX, MIN_Y, minZ), new BlockPos(maxX, MAX_Y, maxZ), range);

        this.start = new BlockPos(this.prepareStart(entity));
        this.maxRange = range;

        this.result = result;
        result.setJob(this);

        allowJumpPointSearchTypeWalk = false;
        this.entity = new WeakReference<>(entity);
    }

    /**
     * AbstractPathJob constructor.
     *
     * @param world            the world within which to path.
     * @param startRestriction start of restricted area.
     * @param endRestriction   end of restricted area.
     * @param result           path result.
     * @param entity           the entity.
     * @see AbstractPathJob#AbstractPathJob(World, BlockPos, BlockPos, int, LivingEntity)
     */
    public AbstractPathJob(final World world, final BlockPos startRestriction, final BlockPos endRestriction, final PathResult result, final LivingEntity entity)
    {
        this.minX = Math.min(startRestriction.getX(), endRestriction.getX());
        this.minZ = Math.min(startRestriction.getZ(), endRestriction.getZ());
        this.maxX = Math.max(startRestriction.getX(), endRestriction.getX());
        this.maxZ = Math.max(startRestriction.getZ(), endRestriction.getZ());

        xzRestricted = true;


        final int range = (int) Math.sqrt(Math.pow(maxX - minX, 2) + Math.pow(maxZ - minZ, 2)) * 2;

        this.world = new ChunkCache(world, new BlockPos(minX, MIN_Y, minZ), new BlockPos(maxX, MAX_Y, maxZ), range);

        this.start = new BlockPos((minX + maxX) / 2, (startRestriction.getY() + endRestriction.getY()) / 2, (minZ + maxZ) / 2);
        this.maxRange = range;

        this.result = result;

        allowJumpPointSearchTypeWalk = false;
        this.entity = new WeakReference<>(entity);
    }

    private static boolean onLadderGoingUp(@NotNull final Node currentNode, @NotNull final BlockPos dPos)
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
    public BlockPos prepareStart(@NotNull final LivingEntity entity)
    {
        return IStartPositionAdapterRegistry.getInstance().getRunner().apply(this, entity).orElse(entity.getPosition());
    }

    /**
     * Sets the direction where the ladder is facing.
     *
     * @param world the world in.
     * @param pos   the position.
     * @param p     the path.
     */
    private static void setLadderFacing(@NotNull final IWorldReader world, final BlockPos pos, @NotNull final PathPointExtended p)
    {
        final BlockState state = world.getBlockState(pos);
        final Block block = state.getBlock();
        if (block instanceof VineBlock)
        {
            if (state.get(VineBlock.SOUTH))
            {
                p.setLadderFacing(Direction.NORTH);
            }
            else if (state.get(VineBlock.WEST))
            {
                p.setLadderFacing(Direction.EAST);
            }
            else if (state.get(VineBlock.NORTH))
            {
                p.setLadderFacing(Direction.SOUTH);
            }
            else if (state.get(VineBlock.EAST))
            {
                p.setLadderFacing(Direction.WEST);
            }
        }
        else if (block instanceof LadderBlock)
        {
            p.setLadderFacing(state.get(LadderBlock.FACING));
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
    private static boolean onALadder(@NotNull final Node node, @Nullable final Node nextInPath, @NotNull final BlockPos pos)
    {
        return nextInPath != null && node.isLadder()
                 &&
                 (nextInPath.pos.getX() == pos.getX() && nextInPath.pos.getZ() == pos.getZ());
    }

    /**
     * Generate a pseudo-unique key for identifying a given node by it's coordinates Encodes the lowest 12 bits of x,z and all useful bits of y. This creates unique keys for all
     * blocks within a 4096x256x4096 cube, which is FAR bigger volume than one should attempt to pathfind within This version takes a BlockPos
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

        if (dPos.getY() != 0 && (dPos.getX() != 0 || dPos.getZ() != 0) && !(Math.abs(dPos.getY()) <= 1 && world.getBlockState(blockPos).getBlock() instanceof StairsBlock))
        {
            //  Tax the cost for jumping, dropping
            cost *= pathingOptions.jumpDropCost * Math.abs(dPos.getY());
        }

        if (world.getBlockState(blockPos).hasProperty(BlockStateProperties.OPEN))
        {
            cost *= pathingOptions.traverseToggleAbleCost;
        }

        if (onPath)
        {
            cost *= pathingOptions.onPathCost;
        }

        if (onRails)
        {
            cost *= pathingOptions.onRailCost;
        }

        if (railsExit)
        {
            cost *= pathingOptions.railsExitCost;
        }

        if (isLadder)
        {
            cost *= pathingOptions.onLadderCost;
        }

        if (isSwimming)
        {
            if (swimStart)
            {
                cost *= pathingOptions.swimCostEnter;
            }
            else
            {
                cost *= pathingOptions.swimCost;
            }
        }

        return cost;
    }

    private static boolean nodeClosed(@Nullable final Node node)
    {
        return node != null && node.isClosed();
    }

    private static boolean calculateSwimming(@NotNull final IWorldReader world, @NotNull final BlockPos pos, @Nullable final Node node)
    {
        return (node == null) ? world.getBlockState(pos.down()).getMaterial().isLiquid() : node.isSwimming();
    }

    public PathResult<AbstractPathJob> getResult()
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
        Node bestNode = getAndSetupStartNode();

        double bestNodeResultScore = Double.MAX_VALUE;

        while (!nodesOpen.isEmpty())
        {
            if (Thread.currentThread().isInterrupted())
            {
                return null;
            }

            final Node currentNode = nodesOpen.poll();

            totalNodesVisited++;

            // Limiting max amount of nodes mapped
            if (totalNodesVisited > ICommonConfig.getInstance().getMaxPathFindingNodes() || totalNodesVisited > maxRange * maxRange)
            {
                break;
            }
            currentNode.setCounterVisited(totalNodesVisited);

            currentNode.setClosed();

            calculationData.onNodeConsumed(currentNode.pos);

            if (isAtDestination(currentNode))
            {
                bestNode = currentNode;
                result.setPathReachesDestination(true);
                break;
            }

            //  If this is the closest node to our destination, treat it as our best node
            final double nodeResultScore =
              getNodeResultScore(currentNode);
            if (nodeResultScore < bestNodeResultScore && !currentNode.isCornerNode()
                  && isWalkableSurface(world.getBlockState(currentNode.pos.down()), currentNode.pos.down()) == SurfaceType.WALKABLE)
            {
                bestNode = currentNode;
                bestNodeResultScore = nodeResultScore;
            }

            if (!xzRestricted || (currentNode.pos.getX() >= minX && currentNode.pos.getX() <= maxX && currentNode.pos.getZ() >= minZ && currentNode.pos.getZ() <= maxZ))
            {
                walkCurrentNode(currentNode);
            }
        }

        @NotNull final Path path = finalizePath(bestNode);

        if (this.entity.get() != null)
        {
            calculationData.onPathCompleted(path);
            if (this.entity.get().getEntityWorld() instanceof ServerWorld)
            {
                final ServerWorld world = (ServerWorld) this.entity.get().getEntityWorld();

                world.getServer().execute(() -> {
                    IPathingResultHandler.getInstance().onCompleted(
                      calculationData,
                      this.entity.get(),
                      world
                    );
                });
            }
        }

        ICalculationResultTracker.getInstance().onCalculationCompleted(this);
        return path;
    }

    private void walkCurrentNode(@NotNull final Node currentNode)
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

        //  We can also go down 1, if the lower block is a ladder
        if (onLadderGoingDown(currentNode, dPos) && pathingOptions.canUseLadders())
        {
            walk(currentNode, BLOCKPOS_DOWN);
        }

        // Only explore downwards when dropping
        if ((currentNode.parent == null || !currentNode.parent.pos.equals(currentNode.pos.down())) && currentNode.isCornerNode())
        {
            walk(currentNode, BLOCKPOS_DOWN);
            return;
        }

        // Walk downwards node if passable
        if (!isNotPassable(currentNode.pos.down(), false))
        {
            walk(currentNode, BLOCKPOS_DOWN);
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

    private boolean onLadderGoingDown(@NotNull final Node currentNode, @NotNull final BlockPos dPos)
    {
        return (dPos.getY() <= 0 || dPos.getX() != 0 || dPos.getZ() != 0) && isLadder(currentNode.pos.down());
    }

    @NotNull
    private Node getAndSetupStartNode()
    {
        @NotNull final Node startNode = new Node(start,
          computeHeuristic(start));

        if (isLadder(start))
        {
            startNode.setLadder();
        }
        else if (world.getBlockState(start.down()).getMaterial().isLiquid())
        {
            startNode.setSwimming();
        }

        startNode.setOnRails(pathingOptions.canUseRails() && world.getBlockState(start).getBlock() instanceof AbstractRailBlock);

        offerNode(startNode, startNode);
        nodesVisited.put(computeNodeKey(start), startNode);

        ++totalNodesAdded;

        return startNode;
    }

    private void offerNode(final Node source, final Node node)
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
    private Path finalizePath(@NotNull final Node targetNode)
    {
        Validate.notNull(targetNode);
        //  Compute length of path, since we need to allocate an array.  This is cheaper/faster than building a List
        //  and converting it.  Yes, we have targetNode.steps, but I do not want to rely on that being accurate (I might
        //  fudge that value later on for cutoff purposes
        int pathLength = 0;
        int railsLength = 0;
        Node node = targetNode;
        while (node.parent != null)
        {
            ++pathLength;
            if (node.isOnRails())
            {
                ++railsLength;
            }
            node = node.parent;
        }

        @NotNull final PathPoint[] points = new PathPoint[pathLength];

        @Nullable Node nextInPath = null;
        node = targetNode;
        while (node.parent != null)
        {
            --pathLength;

            @NotNull final BlockPos pos = node.pos;

            if (node.isSwimming())
            {
                //  Not truly necessary but helps prevent them spinning in place at swimming nodes
                pos.add(BLOCKPOS_DOWN);
            }

            @NotNull final PathPointExtended p = new PathPointExtended(pos);
            if (railsLength >= ICommonConfig.getInstance().getMinimumRailsToUseInPath())
            {
                p.setOnRails(node.isOnRails());
                if (p.isOnRails() && (!node.parent.isOnRails() || node.parent.parent == null))
                {
                    p.setRailsEntry();
                }
                else if (p.isOnRails() && points.length > pathLength + 1)
                {
                    final PathPointExtended point = ((PathPointExtended) points[pathLength + 1]);
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
                if (nextInPath.pos.getY() > pos.getY())
                {
                    //  We only care about facing if going up
                    //In the case of BlockVines (Which does not have Direction) we have to check the metadata of the vines... bitwise...
                    setLadderFacing(world, pos, p);
                }
            }
            else if (onALadder(node.parent, node.parent, pos))
            {
                p.setOnLadder(true);
            }

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
    protected BlockPos getNodePosition(final Node node)
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
    protected abstract boolean isAtDestination(Node n);

    /**
     * Compute a 'result score' for the Node; if no destination is determined, the node that had the highest 'result' score is used.
     *
     * @param n Node to test.
     * @return score for the node.
     */
    protected abstract double getNodeResultScore(Node n);

    /**
     * "Walk" from the parent in the direction specified by the delta, determining the new x,y,z position for such a move and adding or updating a node, as appropriate.
     *
     * @param parent Node being walked from.
     * @param dPos   Delta from parent, expected in range of [-1..1].
     */
    protected final void walk(@NotNull final Node parent, @NotNull BlockPos dPos)
    {
        BlockPos pos = parent.pos.add(dPos);

        //  Can we traverse into this node?  Fix the y up
        final int newY = getGroundHeight(parent, pos);

        if (newY < 0)
        {
            return;
        }

        boolean corner = false;
        if (pos.getY() != newY)
        {
            // if the new position is above the current node, we're taking the node directly above
            if (!parent.isCornerNode() && newY - pos.getY() > 0 && (parent.parent == null || !parent.parent.pos.equals(parent.pos.add(new BlockPos(0, newY - pos.getY(), 0)))))
            {
                dPos = new BlockPos(0, newY - pos.getY(), 0);
                pos = parent.pos.add(dPos);
                corner = true;
            }
            // If we're going down, take the air-corner before going to the lower node
            else if (!parent.isCornerNode() && newY - pos.getY() < 0 && (dPos.getX() != 0 || dPos.getZ() != 0) && (parent.parent == null || !parent.pos.down()
                                                                                                                                               .equals(parent.parent.pos)))
            {
                dPos = new BlockPos(dPos.getX(), 0, dPos.getZ());
                pos = parent.pos.add(dPos);
                corner = true;
            }
            // Fix up normal y
            else
            {
                dPos = dPos.add(0, newY - pos.getY(), 0);
                pos = new BlockPos(pos.getX(), newY, pos.getZ());
            }
        }

        int nodeKey = computeNodeKey(pos);
        Node node = nodesVisited.get(nodeKey);
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
        final boolean onRoad = IRoadBlockRegistry.getInstance().getRunner().isRoad(this.entity.get(), world.getBlockState(pos.down()).getBlock());
        final boolean onRails = pathingOptions.canUseRails() && world.getBlockState(corner ? pos.down() : pos).getBlock() instanceof AbstractRailBlock;
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

    private void performJumpPointSearch(@NotNull final Node parent, @NotNull final BlockPos dPos, @NotNull final Node node)
    {
        if (allowJumpPointSearchTypeWalk && node.getHeuristic() <= parent.getHeuristic())
        {
            walk(node, dPos);
        }
    }

    @NotNull
    private Node createNode(
      final Node parent, @NotNull final BlockPos pos, final int nodeKey,
      final boolean isSwimming, final double heuristic, final double cost, final double score)
    {
        final Node node;
        node = new Node(parent, pos, cost, heuristic, score);
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

    private boolean updateCurrentNode(@NotNull final Node parent, @NotNull final Node node, final double heuristic, final double cost, final double score)
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
     * @return y height of first open, viable block above ground, or -1 if blocked or too far a drop.
     */
    protected int getGroundHeight(final Node parent, @NotNull final BlockPos pos)
    {
        //  Check (y+1) first, as it's always needed, either for the upper body (level),
        //  lower body (headroom drop) or lower body (jump up)
        if (checkHeadBlock(parent, pos))
        {
            return handleTargetNotPassable(parent, pos.up(), world.getBlockState(pos.up()));
        }

        //  Now check the block we want to move to
        final BlockState target = world.getBlockState(pos);
        if (isNotPassable(pos, false))
        {
            return handleTargetNotPassable(parent, pos, target);
        }

        //  Do we have something to stand on in the target space?
        final BlockState below = world.getBlockState(pos.down());
        final SurfaceType walkability = isWalkableSurface(below, pos);
        if (walkability == SurfaceType.WALKABLE)
        {
            //  Level path
            return pos.getY();
        }
        else if (walkability == SurfaceType.NOT_PASSABLE)
        {
            return -1;
        }

        return handleNotStanding(parent, pos, below);
    }

    private int handleNotStanding(@Nullable final Node parent, @NotNull final BlockPos pos, @NotNull final BlockState below)
    {
        final boolean isSwimming = parent != null && parent.isSwimming();

        if (below.getMaterial().isLiquid())
        {
            return handleInLiquid(pos, below, isSwimming);
        }

        if (isLadder(below.getBlock(), pos.down()))
        {
            return pos.getY();
        }

        return checkDrop(parent, pos, isSwimming);
    }

    private int checkDrop(@Nullable final Node parent, @NotNull final BlockPos pos, final boolean isSwimming)
    {
        final boolean canDrop = parent != null && !parent.isLadder();
        //  Nothing to stand on
        if (!canDrop || isSwimming || ((parent.pos.getX() != pos.getX() || parent.pos.getZ() != pos.getZ()) && !isNotPassable(parent.pos.down(), false)
                                         && isWalkableSurface(world.getBlockState(parent.pos.down()), parent.pos.down()) == SurfaceType.DROPABLE))
        {
            return -1;
        }

        for (int i = 2; i <= 10; i++)
        {
            final BlockState below = world.getBlockState(pos.down(i));
            if (isWalkableSurface(below, pos) == SurfaceType.WALKABLE && i <= 4 || below.getMaterial().isLiquid())
            {
                //  Level path
                return pos.getY() - i + 1;
            }
            else if (below.getMaterial() != Material.AIR)
            {
                return -1;
            }
        }

        return -1;
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
            //  This is water, and we are allowed to swim
            return pos.getY();
        }

        //  Not allowed to swim or this isn't water, and we're on dry land
        return -1;
    }

    private int handleTargetNotPassable(@Nullable final Node parent, @NotNull final BlockPos pos, @NotNull final BlockState target)
    {
        final boolean canJump = parent != null && !parent.isLadder() && !parent.isSwimming();
        //  Need to try jumping up one, if we can
        if (!canJump || isWalkableSurface(target, pos) != SurfaceType.WALKABLE)
        {
            return -1;
        }

        //  Check for headroom in the target space
        if (isNotPassable(pos.up(2), false))
        {
            final VoxelShape bb1 = world.getBlockState(pos).getCollisionShape(world, pos);
            final VoxelShape bb2 = world.getBlockState(pos.up(2)).getCollisionShape(world, pos.up(2));
            if ((pos.up(2).getY() + getStartY(bb2)) - (pos.getY() + getEndY(bb1)) < 2)
            {
                return -1;
            }
        }

        //  Check for jump room from the origin space
        if (isNotPassable(parent.pos.up(2), false))
        {
            final VoxelShape bb1 = world.getBlockState(pos).getCollisionShape(world, pos);
            final VoxelShape bb2 = world.getBlockState(parent.pos.up(2)).getCollisionShape(world, parent.pos.up(2));
            if ((parent.pos.up(2).getY() + getStartY(bb2)) - (pos.getY() + getEndY(bb1)) < 2)
            {
                return -1;
            }
        }

        final BlockState parentBelow = world.getBlockState(parent.pos.down());
        final VoxelShape parentBB = parentBelow.getCollisionShape(world, parent.pos.down());

        double parentY = parentBB.getEnd(Direction.Axis.Y);
        double parentMaxY = parentY + parent.pos.down().getY();
        final double targetMaxY = target.getCollisionShape(world, pos).getEnd(Direction.Axis.Y) + pos.getY();
        if (targetMaxY - parentMaxY < MAX_JUMP_HEIGHT)
        {
            return pos.getY() + 1;
        }
        if (target.getBlock() instanceof StairsBlock
              && parentY - HALF_A_BLOCK < MAX_JUMP_HEIGHT
              && target.get(StairsBlock.HALF) == Half.BOTTOM
              && getXZFacing(parent.pos, pos) == target.get(StairsBlock.FACING))
        {
            return pos.getY() + 1;
        }
        return -1;
    }

    private boolean checkHeadBlock(@Nullable final Node parent, @NotNull final BlockPos pos)
    {
        BlockPos localPos = pos;
        final VoxelShape bb = world.getBlockState(localPos).getCollisionShape(world, localPos);
        if (bb.isEmpty())
        {
            return false;
        }

        if (bb.getEnd(Direction.Axis.Y) < 1)
        {
            localPos = pos.up();
        }

        if (isNotPassable(pos.up(), true))
        {
            final VoxelShape bb1 = world.getBlockState(pos.down()).getCollisionShape(world, pos.down());
            final VoxelShape bb2 = world.getBlockState(pos.up()).getCollisionShape(world, pos.up());
            if ((pos.up().getY() + getStartY(bb2)) - (pos.down().getY() + getEndY(bb1)) < 2)
            {
                return true;
            }
            if (parent != null)
            {
                final VoxelShape bb3 = world.getBlockState(parent.pos.down()).getCollisionShape(world, pos.down());
                if ((pos.up().getY() + getStartY(bb2)) - (parent.pos.down().getY() + getEndY(bb3)) < 1.75)
                {
                    return true;
                }
            }
        }

        if (parent != null)
        {
            final BlockState hereState = world.getBlockState(localPos.down());

            final VoxelShape bb1 = world.getBlockState(pos).getCollisionShape(world, pos);
            final VoxelShape bb2 = world.getBlockState(localPos.up()).getCollisionShape(world, localPos.up());
            if ((localPos.up().getY() + getStartY(bb2)) - (pos.getY() + getEndY(bb1)) >= 2)
            {
                return false;
            }

            return hereState.getMaterial().isLiquid() && isNotPassable(pos, false);
        }
        return false;
    }

    /**
     * Get the start y of a voxelshape.
     *
     * @param bb the voxelshape.
     * @return the start y.
     */
    private double getStartY(final VoxelShape bb)
    {
        return bb.isEmpty() ? 1 : bb.getStart(Direction.Axis.Y);
    }

    /**
     * Get the end y of a voxelshape.
     *
     * @param bb the voxelshape.
     * @return the end y.
     */
    private double getEndY(final VoxelShape bb)
    {
        return bb.isEmpty() ? 0 : bb.getEnd(Direction.Axis.Y);
    }

    protected boolean isNotPassable(final BlockPos pos, final boolean head)
    {
        final BlockState blockState = world.getBlockState(pos);
        if (blockState.getMaterial() != Material.AIR)
        {
            return IPassableBlockRegistry.getInstance()
                     .getRunner()
                     .isPassable(this.pathingOptions, this.entity.get(), blockState, head).orElseGet(() -> {
                  if (blockState.getMaterial().blocksMovement())
                  {
                      return !(pathingOptions.canUseDynamicPassableBlocks() && (blockState.getBlock() instanceof DoorBlock
                                                                                || blockState.getBlock() instanceof FenceGateBlock
                                                                                || (blockState.getBlock() instanceof TrapDoorBlock && !(world.getBlockState(pos.up()).getBlock() instanceof TrapDoorBlock)))
                               || blockState.getBlock() instanceof PressurePlateBlock
                               || blockState.getBlock() instanceof AbstractSignBlock
                               || blockState.getBlock() instanceof AbstractBannerBlock);
                  }
                  else if (blockState.getBlock() instanceof FireBlock)
                  {
                      return true;
                  }
                  else
                  {
                      final VoxelShape shape = blockState.getCollisionShape(world, pos);
                      return !(isLadder(blockState.getBlock(), pos) ||
                               ((shape.isEmpty() || shape.getEnd(Direction.Axis.Y) <= 0.1)
                                  && !blockState.getMaterial().isLiquid()
                                  && (blockState.getBlock() != Blocks.SNOW || blockState.get(SnowBlock.LAYERS) == 1)
                                  && blockState.getBlock() != Blocks.SWEET_BERRY_BUSH));
                  }
              });
        }

        return false;
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
                  || (blockState.getShape(world, pos).getEnd(Direction.Axis.Y) > 1.0))
            {
                return SurfaceType.NOT_PASSABLE;
            }

            final FluidState fluid = world.getFluidState(pos);
            if (!fluid.isEmpty() && (fluid.getFluid() == Fluids.LAVA || fluid.getFluid() == Fluids.FLOWING_LAVA))
            {
                return SurfaceType.NOT_PASSABLE;
            }

            if (blockState.getMaterial().isSolid() || (blockState.getBlock() == Blocks.SNOW && blockState.get(SnowBlock.LAYERS) > 1)
                  || blockState.getBlock() instanceof CarpetBlock)
            {
                return SurfaceType.WALKABLE;
            }

            return SurfaceType.DROPABLE;
        });
    }

    /**
     * Is the block a ladder.
     *
     * @param block block to check.
     * @param pos   location of the block.
     * @return true if the block is a ladder.
     */
    protected boolean isLadder(@NotNull final Block block, final BlockPos pos)
    {
        return block.isLadder(this.world.getBlockState(pos), world, pos, entity.get());
    }

    protected boolean isLadder(final BlockPos pos)
    {
        return isLadder(world.getBlockState(pos).getBlock(), pos);
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

    public static Direction getXZFacing(final BlockPos pos, final BlockPos neighbor)
    {
        final BlockPos vector = neighbor.subtract(pos);
        return Direction.getFacingFromVector(vector.getX(), 0, vector.getZ());
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
