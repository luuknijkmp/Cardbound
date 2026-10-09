package mctcg.card;

import mctcg.ModComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import mctcg.essence.PlayerEssence;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import mctcg.network.CardActivationPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

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

        // Card abilities are handled on the server.
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        var cardId = stack.get(ModComponents.CARD_ID);

        var definitionOptional = CardRegistry.get(cardId);

        if (definitionOptional.isEmpty()) {
            return InteractionResult.FAIL;
        }

        CardDefinition definition = definitionOptional.get();

// Cards without abilities cannot be activated.
        if (definition.ability().isEmpty()) {
            return InteractionResult.FAIL;
        }

        CardAbility ability = definition.ability().get();

// For now, we only support potion-effect abilities.
        Identifier applyEffectType =
                Identifier.fromNamespaceAndPath("mctcg", "apply_effect");

        if (!ability.type().equals(applyEffectType)) {
            return InteractionResult.FAIL;
        }

// Find the Minecraft effect specified in the card's JSON.
        var effectOptional = BuiltInRegistries.MOB_EFFECT.get(ability.effect());

        if (effectOptional.isEmpty()) {
            return InteractionResult.FAIL;
        }

// Reject invalid ability settings.
        if (definition.cost() < 0 || ability.duration() <= 0
                || ability.amplifier() < 0 || ability.amplifier() > 255) {
            return InteractionResult.FAIL;
        }

// Check whether the player can afford the ability.
        int currentEssence = player.getAttachedOrCreate(PlayerEssence.ESSENCE);
        int cost = definition.cost();

        if (currentEssence < cost) {
            return InteractionResult.FAIL;
        }

// Apply the effect.
        MobEffectInstance effect = new MobEffectInstance(
                effectOptional.get(),
                ability.duration(),
                ability.amplifier()
        );

        boolean applied = player.addEffect(effect);

        if (!applied) {
            return InteractionResult.FAIL;
        }

// Only spend Essence after successful activation.
        player.setAttached(PlayerEssence.ESSENCE, currentEssence - cost);

// Play the card activation animation for the player.
        if (player instanceof ServerPlayer serverPlayer) {
            ServerPlayNetworking.send(
                    serverPlayer,
                    new CardActivationPayload(hand == InteractionHand.OFF_HAND)
            );
        }

        return InteractionResult.SUCCESS;
    }
}