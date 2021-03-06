package com.ldtteam.perviaminvenire.network.message;

import com.ldtteam.perviaminvenire.api.pathfinding.PathingCalculationData;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;

public class OnCalculationCompleted
{
    private final UUID entityId;
    private final PathingCalculationData data;

    public OnCalculationCompleted(final UUID entityId, final PathingCalculationData data) {
        this.entityId = entityId;
        this.data = data;
    }

    public OnCalculationCompleted(final PacketBuffer buffer) {
        this(buffer.readUniqueId(), new PathingCalculationData());
        this.data.fromPacketBuffer(buffer);
    }

    public void write(final PacketBuffer buffer) {
        buffer.writeUniqueId(this.entityId);
        this.data.toPacketBuffer(buffer);
    }

    public void processPacket(final NetworkEvent.Context ctx, final boolean onClient)
    {
    }
}
