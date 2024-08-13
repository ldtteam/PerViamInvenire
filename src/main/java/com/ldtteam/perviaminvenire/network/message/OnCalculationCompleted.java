package com.ldtteam.perviaminvenire.network.message;

import com.ldtteam.perviaminvenire.api.pathfinding.PathingCalculationData;
import com.ldtteam.perviaminvenire.api.util.constants.ModConstants;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record OnCalculationCompleted(UUID entityId, PathingCalculationData data) implements CustomPacketPayload
{

    public static final CustomPacketPayload.Type<OnCalculationCompleted> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ModConstants.MOD_ID, "on_calculation_completed"));

    public static final StreamCodec<FriendlyByteBuf, OnCalculationCompleted> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            OnCalculationCompleted::entityId,
            PathingCalculationData.STREAM_CODEC,
            OnCalculationCompleted::data,
            OnCalculationCompleted::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
