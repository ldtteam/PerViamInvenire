package com.ldtteam.perviaminvenire.api.pathfinding;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import net.minecraft.network.PacketBuffer;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PathingCalculationData
{
    private final Multimap<BlockPos, BlockPos>          walkedPositions    = HashMultimap.create();
    private final Map<BlockPos, InvalidNodeReason> invalidNodeReasons = Maps.newHashMap();
    private final LinkedList<BlockPos>                  consumedNodes      = new LinkedList<>();
    private final LinkedList<BlockPos>                  path               = new LinkedList<>();
    private       boolean                               reachesDestination = false;

    public PathingCalculationData()
    {
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
        path.points.stream()
          .map(point -> new BlockPos(point.x, point.y, point.z))
          .forEach(this.path::add);

        this.reachesDestination = path.reachesTarget();
    }

    public void reset()
    {
        this.walkedPositions.clear();
        this.invalidNodeReasons.clear();
        this.consumedNodes.clear();
        this.path.clear();
        this.reachesDestination = false;
    }

    public void fromPacketBuffer(final PacketBuffer buffer)
    {
        reset();

        final int walkedPositionSourceCount = buffer.readVarInt();
        for (int i = 0; i < walkedPositionSourceCount; i++)
        {
            final BlockPos source = buffer.readBlockPos();
            final int targetCount = buffer.readVarInt();
            for (int j = 0; j < targetCount; j++)
            {
                this.walkedPositions.put(
                  source,
                  buffer.readBlockPos()
                );
            }
        }

        final int invalidReasonSourceCount = buffer.readVarInt();
        for (int i = 0; i < invalidReasonSourceCount; i++)
        {
            final BlockPos source = buffer.readBlockPos();
            this.invalidNodeReasons.put(
              source,
              InvalidNodeReason.values()[buffer.readVarInt()]
            );
        }

        final int consumedNodeCount = buffer.readVarInt();
        for (int i = 0; i < consumedNodeCount; i++)
        {
            this.consumedNodes.add(buffer.readBlockPos());
        }

        final int pathNodeCount = buffer.readVarInt();
        for (int i = 0; i < pathNodeCount; i++)
        {
            this.path.add(buffer.readBlockPos());
        }

        this.reachesDestination = buffer.readBoolean();
    }

    public void toPacketBuffer(final PacketBuffer buffer)
    {
        buffer.writeVarInt(this.walkedPositions.keySet().size());
        this.walkedPositions.keySet().forEach(source -> {
            buffer.writeBlockPos(source);
            buffer.writeVarInt(this.walkedPositions.get(source).size());
            this.walkedPositions.get(source).forEach(buffer::writeBlockPos);
        });

        buffer.writeVarInt(this.invalidNodeReasons.keySet().size());
        this.invalidNodeReasons.keySet().forEach(source -> {
            buffer.writeBlockPos(source);
            buffer.writeVarInt(this.invalidNodeReasons.get(source).ordinal());
        });

        buffer.writeVarInt(this.consumedNodes.size());
        this.consumedNodes.forEach(buffer::writeBlockPos);

        buffer.writeVarInt(this.path.size());
        this.path.forEach(buffer::writeBlockPos);

        buffer.writeBoolean(this.reachesDestination);
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