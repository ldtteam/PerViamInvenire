package com.ldtteam.perviaminvenire.network.message;

import com.ldtteam.perviaminvenire.api.pathfinding.PathingCalculationData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;

public class OnCalculationCompleted
{
    private final UUID entityId;
    private final PathingCalculationData data;

    public OnCalculationCompleted(final UUID entityId, final PathingCalculationData data) {
        this.entityId = entityId;
        this.data = data;
    }

    public OnCalculationCompleted(final FriendlyByteBuf buffer) {
        this(buffer.readUUID(), new PathingCalculationData());
        this.data.fromPacketBuffer(buffer);
    }

    public void write(final FriendlyByteBuf buffer) {
        buffer.writeUUID(this.entityId);
        this.data.toPacketBuffer(buffer);
    }

    public void processPacket(final NetworkEvent.Context ctx, final boolean onClient)
    {
    }
}
