package cardbound.card;

import cardbound.ModComponents;
import cardbound.ModSounds;
import cardbound.card.ability.AbilityExecutor;
import cardbound.network.CardActivationPayload;
import cardbound.network.CardRevealPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import cardbound.card.CardActivationRing;
import net.minecraft.sounds.SoundSource;

public class CardItem extends Item {

    public CardItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(
            Level level,
            Player player,
            InteractionHand hand
    ) {
        ItemStack stack = player.getItemInHand(hand);

        // Card abilities and previews are handled on the server.
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        // Shift + right-click opens the card preview.
        if (player.isShiftKeyDown()) {
            if (player instanceof ServerPlayer serverPlayer) {
                ServerPlayNetworking.send(
                        serverPlayer,
                        new CardRevealPayload(stack.copy(), true)
                );
            }

            return InteractionResult.SUCCESS;
        }

        var cardId = stack.get(ModComponents.CARD_ID);

        if (cardId == null) {
            return InteractionResult.FAIL;
        }

        var definitionOptional = CardRegistry.get(cardId);

        if (definitionOptional.isEmpty()) {
            return InteractionResult.FAIL;
        }

        CardDefinition definition = definitionOptional.get();

        if (!AbilityExecutor.activate(player, definition)) {
            return InteractionResult.FAIL;
        }

        // Play the existing activation animation.
        // Play activation feedback after a successful ability.
        if (player instanceof ServerPlayer serverPlayer) {
            CardActivationRing.start(serverPlayer);

            float pitch = 0.95F
                    + serverPlayer.getRandom().nextFloat() * 0.10F;

            serverPlayer.level().playSound(
                    null,
                    serverPlayer.getX(),
                    serverPlayer.getY(),
                    serverPlayer.getZ(),
                    ModSounds.CARD_ACTIVATE,
                    SoundSource.AMBIENT,
                    1.0F,
                    pitch
            );

            ServerPlayNetworking.send(
                    serverPlayer,
                    new CardActivationPayload(hand == InteractionHand.OFF_HAND)
            );
        }

        return InteractionResult.SUCCESS;
    }
}