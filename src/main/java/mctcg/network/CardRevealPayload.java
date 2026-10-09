package mctcg.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public record CardRevealPayload(ItemStack card)
        implements CustomPacketPayload {

    public static final Type<CardRevealPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(
                    "mctcg", "card_reveal"
            ));

    public static final StreamCodec<RegistryFriendlyByteBuf, CardRevealPayload> CODEC =
            StreamCodec.of(
                    (buffer, payload) ->
                            ItemStack.STREAM_CODEC.encode(buffer, payload.card()),

                    buffer ->
                            new CardRevealPayload(
                                    ItemStack.STREAM_CODEC.decode(buffer)
                            )
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}