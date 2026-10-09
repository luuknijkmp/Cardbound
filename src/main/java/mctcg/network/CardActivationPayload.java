package mctcg.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record CardActivationPayload(boolean offhand)
        implements CustomPacketPayload {

    public static final Type<CardActivationPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(
                    "mctcg", "card_activation"
            ));

    public static final StreamCodec<RegistryFriendlyByteBuf, CardActivationPayload> CODEC =
            StreamCodec.of(
                    (buffer, payload) -> buffer.writeBoolean(payload.offhand()),
                    buffer -> new CardActivationPayload(buffer.readBoolean())
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}