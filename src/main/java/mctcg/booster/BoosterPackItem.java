package mctcg.booster;

import mctcg.ModComponents;
import mctcg.ModSounds;
import mctcg.network.CardRevealPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemContainerContents;
import mctcg.booster.BoosterDefinition;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.ItemLore;
import java.util.ArrayList;
import java.util.List;

public class BoosterPackItem extends Item {

    public BoosterPackItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(
            Level level,
            Player player,
            InteractionHand hand
    ) {
        ItemStack stack = player.getItemInHand(hand);

        // Only the server should generate and award rewards.
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        var boosterId = stack.get(ModComponents.PACK_ID);

        // An unidentified booster cannot be opened.
        if (boosterId == null || !BoosterRegistry.exists(boosterId)) {
            return InteractionResult.FAIL;
        }

        var definition = BoosterRegistry.get(boosterId).orElseThrow();

        // Check whether this booster already has generated rewards.
        ItemContainerContents contents = stack.get(DataComponents.CONTAINER);

        // Generate the rewards only on the first opening.
        if (contents == null) {
            List<ItemStack> rewards = BoosterPackFactory.openPack(
                    boosterId,
                    player.getRandom()
            );

            // Don't consume the booster if no cards were generated.
            if (rewards.isEmpty()) {
                return InteractionResult.FAIL;
            }

            contents = ItemContainerContents.fromItems(rewards);
        }

        // Make a modifiable list of the cards still inside.
        List<ItemStack> remaining = new ArrayList<>(
                contents.nonEmptyItemCopyStream().toList()
        );

        // No cards left to reveal.
        if (remaining.isEmpty()) {
            return InteractionResult.FAIL;
        }

        // Take the next card out of the booster.
        ItemStack revealedCard = remaining.remove(0);
        ItemStack cardForReveal = revealedCard.copy();

        // Give the revealed card before changing or consuming the booster.
        if (!player.getInventory().add(revealedCard)) {
            player.drop(revealedCard, false);
        }

        // Tell the player's client to display the revealed card.
        if (player instanceof ServerPlayer serverPlayer) {
            ServerPlayNetworking.send(
                    serverPlayer,
                    new CardRevealPayload(cardForReveal)
            );
        }

        float pitch = 0.95F + player.getRandom().nextFloat() * 0.10F;

        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                ModSounds.CARD_UNPACKED,
                player.getSoundSource(),
                1.0F,
                pitch
        );

        // Calculate the maximum number of cards from the booster JSON.
        int maxCards = definition.slots()
                .stream()
                .mapToInt(BoosterSlot::count)
                .sum();

        if (remaining.isEmpty()) {
            // The final card was revealed. Consume the booster.
            // This also applies in Creative to prevent reward rerolls.
            stack.shrink(1);
        } else {
            // Save the cards that haven't been revealed yet.
            stack.set(
                    DataComponents.CONTAINER,
                    ItemContainerContents.fromItems(remaining)
            );

            // Update the booster lore.
            stack.set(
                    DataComponents.LORE,
                    new ItemLore(List.of(
                            Component.literal("Cards: ")
                                    .withStyle(ChatFormatting.GRAY)
                                    .append(
                                            Component.literal(
                                                    remaining.size() + "/" + maxCards
                                            ).withStyle(ChatFormatting.WHITE)
                                    )
                                    .withStyle(style -> style.withItalic(false))
                    ))
            );
        }

        return InteractionResult.SUCCESS;
    }
}