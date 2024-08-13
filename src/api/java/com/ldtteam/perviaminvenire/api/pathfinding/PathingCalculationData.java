package com.ldtteam.perviaminvenire.api.pathfinding;

import com.google.common.collect.*;
import com.jcraft.jorbis.Block;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.pathfinder.Path;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

import java.util.*;
import java.util.function.Function;
import java.util.function.IntFunction;

public class PathingCalculationData
{
    public static final StreamCodec<FriendlyByteBuf, PathingCalculationData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(
                    (IntFunction<Map<BlockPos, Collection<BlockPos>>>) Maps::newHashMapWithExpectedSize,
                    BlockPos.STREAM_CODEC,
                    ByteBufCodecs.collection((IntFunction<Collection<BlockPos>>) ArrayList::new).apply(BlockPos.STREAM_CODEC)
            ).map(
                    blockPosCollectionMap -> {
                        final Multimap<BlockPos, BlockPos> multimap = HashMultimap.create();
                        blockPosCollectionMap.forEach(multimap::putAll);
                        return multimap;
                    },
                    Multimaps::asMap
            ),
            PathingCalculationData::getWalkedPositions,
            ByteBufCodecs.map(
                    Maps::newHashMapWithExpectedSize,
                    BlockPos.STREAM_CODEC,
                    NeoForgeStreamCodecs.enumCodec(InvalidNodeReason.class)
            ),
            PathingCalculationData::getInvalidNodeReasons,
            ByteBufCodecs.collection((IntFunction<LinkedList<BlockPos>>) value -> new LinkedList<>()).apply(BlockPos.STREAM_CODEC),
            PathingCalculationData::getConsumedNodes,
            ByteBufCodecs.collection((IntFunction<LinkedList<BlockPos>>) value -> new LinkedList<>()).apply(BlockPos.STREAM_CODEC),
            PathingCalculationData::getPath,
            ByteBufCodecs.BOOL,
            PathingCalculationData::isReachesDestination,
            PathingCalculationData::new
    );

    private final Multimap<BlockPos, BlockPos>          walkedPositions;
    private final Map<BlockPos, InvalidNodeReason>      invalidNodeReasons;
    private final LinkedList<BlockPos>                  consumedNodes;
    private final LinkedList<BlockPos>                  path;
    private       boolean                               reachesDestination;

    private PathingCalculationData(
      final Multimap<BlockPos, BlockPos> walkedPositions,
      final Map<BlockPos, InvalidNodeReason> invalidNodeReasons,
      final LinkedList<BlockPos> consumedNodes,
      final LinkedList<BlockPos> path,
      final boolean reachesDestination
    ) {
        this.walkedPositions = walkedPositions;
        this.invalidNodeReasons = invalidNodeReasons;
        this.consumedNodes = consumedNodes;
        this.path = path;
        this.reachesDestination = reachesDestination;
    }

    public PathingCalculationData()
    {
        this(
          HashMultimap.create(),
          Maps.newHashMap(),
          Lists.newLinkedList(),
          Lists.newLinkedList(),
          false
        );
    }

    public void onNodeWalked(final BlockPos source, final BlockPos target)
    {
        walkedPositions.put(source, target);
    }

    public void onInvalidNode(final BlockPos invalid, final InvalidNodeReason reason)
    {
        invalidNodeReasons.put(invalid, reason);
    }

    public void onNodeConsumed(final BlockPos consumed)
    {
        consumedNodes.add(consumed);
    }

    public void onPathCompleted(final Path path)
    {
        path.nodes.stream()
          .map(point -> new BlockPos(point.x, point.y, point.z))
          .forEach(this.path::add);

        this.reachesDestination = path.canReach();
    }

    public void reset()
    {
        this.walkedPositions.clear();
        this.invalidNodeReasons.clear();
        this.consumedNodes.clear();
        this.path.clear();
        this.reachesDestination = false;
    }

    public Multimap<BlockPos, BlockPos> getWalkedPositions()
    {
        return walkedPositions;
    }

    public Map<BlockPos, InvalidNodeReason> getInvalidNodeReasons()
    {
        return invalidNodeReasons;
    }

    public LinkedList<BlockPos> getConsumedNodes()
    {
        return consumedNodes;
    }

    public LinkedList<BlockPos> getPath()
    {
        return path;
    }

    public boolean isReachesDestination()
    {
        return reachesDestination;
    }

    public enum InvalidNodeReason
    {
        SWIMMING_NODE
    }
}
