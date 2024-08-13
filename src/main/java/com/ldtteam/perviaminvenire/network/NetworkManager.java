package com.ldtteam.perviaminvenire.network;

import com.ldtteam.perviaminvenire.api.util.constants.ModConstants;
import com.ldtteam.perviaminvenire.network.message.OnCalculationCompleted;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = ModConstants.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class NetworkManager
{
    private static final String        LATEST_PROTO_VER    = "1.0";
    private static final String        ACCEPTED_PROTO_VERS = LATEST_PROTO_VER;

    private static final NetworkManager INSTANCE = new NetworkManager();

    public static NetworkManager getInstance()
    {
        return INSTANCE;
    }

    private NetworkManager()
    {
    }

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.commonToClient(
                OnCalculationCompleted.TYPE,
                OnCalculationCompleted.CODEC,
                (payload, context) -> {
                    //TODO Handle this.
                }
        );
    }

    public <T extends CustomPacketPayload> void sendToPlayer(final T packet, final ServerPlayer... players)
    {
        for (final ServerPlayer player : players)
        {
            if (player.connection.hasChannel(OnCalculationCompleted.TYPE)) {
                player.connection.send(packet);
            }
        }
    }
}
