package mctcg.booster;

import mctcg.card.CardFactory;
import mctcg.card.CardOrigin;
import mctcg.card.CardRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class BoosterPackFactory {

    public static List<ItemStack> openPack(
            Identifier boosterId,
            RandomSource random
    ) {
        // Find the booster definition.
        var boosterOptional = BoosterRegistry.get(boosterId);

        if (boosterOptional.isEmpty()) {
            return List.of();
        }

        BoosterDefinition booster = boosterOptional.get();

        // Find cards that belong to this booster's set.
        List<Identifier> availableCards = CardRegistry.getAllIds()
                .stream()
                .filter(cardId -> CardRegistry.get(cardId)
                        .map(card -> card.set().equals(booster.set()))
                        .orElse(false))
                .toList();

        List<ItemStack> rewards = new ArrayList<>();

        if (availableCards.isEmpty()) {
            return rewards;
        }

        // Generate the configured number of cards.
        for (int i = 0; i < booster.cardsPerPack(); i++) {
            int randomIndex = random.nextInt(availableCards.size());

            Identifier selectedCard = availableCards.get(randomIndex);

            ItemStack card = CardFactory.createCard(
                    selectedCard,
                    CardOrigin.BOOSTER_PACK
            );

            rewards.add(card);
        }

        return rewards;
    }
}