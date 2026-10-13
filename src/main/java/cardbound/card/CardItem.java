package cardbound.card;

import cardbound.ModComponents;
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
        if (player instanceof ServerPlayer serverPlayer) {
            ServerPlayNetworking.send(
                    serverPlayer,
                    new CardActivationPayload(hand == InteractionHand.OFF_HAND)
            );
        }

        return InteractionResult.SUCCESS;
    }
}