package com.ldtteam.perviaminvenire.network;

import com.ldtteam.perviaminvenire.api.util.constants.ModConstants;
import com.ldtteam.perviaminvenire.network.message.OnCalculationCompleted;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class NetworkManager
{
    private static final String        LATEST_PROTO_VER    = "1.0";
    private static final String        ACCEPTED_PROTO_VERS = LATEST_PROTO_VER;

    private static final NetworkManager INSTANCE = new NetworkManager();

    public static NetworkManager getInstance()
    {
        return INSTANCE;
    }

    private SimpleChannel channel = null;

    private NetworkManager()
    {
    }

    public void initialize() {
        this.channel = NetworkRegistry.newSimpleChannel(new ResourceLocation(ModConstants.MOD_ID, ModConstants.MOD_ID), () -> LATEST_PROTO_VER, ACCEPTED_PROTO_VERS::equals, ACCEPTED_PROTO_VERS::equals);

        this.channel.registerMessage(0, OnCalculationCompleted.class, OnCalculationCompleted::write, OnCalculationCompleted::new, (msg, ctxIn) -> {
            final NetworkEvent.Context ctx = ctxIn.get();
            final LogicalSide packetOrigin = ctx.getDirection().getOriginationSide();
            ctx.setPacketHandled(true);
            // boolean param MUST equals true if packet arrived at logical server
            ctx.enqueueWork(() -> msg.processPacket(ctx, packetOrigin.equals(LogicalSide.CLIENT)));
        });
    }

    public <T> void sendToPlayer(final T packet, final ServerPlayerEntity... players)
    {
        for (final ServerPlayerEntity player : players)
        {
            this.channel.send(PacketDistributor.PLAYER.with(() -> player), packet);
        }
    }
}
