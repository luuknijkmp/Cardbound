package mctcg.booster;

import mctcg.ModComponents;
import mctcg.card.CardDefinition;
import mctcg.card.CardFactory;
import mctcg.card.CardOrigin;
import mctcg.card.CardRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import java.util.*;

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

        Map<String, List<Identifier>> cardsByRarity = new HashMap<>();

        for (Identifier cardId : availableCards) {
            CardDefinition definition = CardRegistry.get(cardId).orElseThrow();

            cardsByRarity
                    .computeIfAbsent(
                            definition.rarity(),
                            rarity -> new ArrayList<>()
                    )
                    .add(cardId);
        }

        List<ItemStack> rewards = new ArrayList<>();

        if (availableCards.isEmpty()) {
            return rewards;
        }

        // Generate the configured number of cards.
        for (BoosterSlot slot : booster.slots()) {

            for (int i = 0; i < slot.count(); i++) {

                String selectedRarity = chooseRarity(
                        cardsByRarity,
                        booster.rarityWeights(),
                        slot.minimumRarity(),
                        random
                );

                if (selectedRarity == null) {
                    continue;
                }

                List<Identifier> rarityCards = cardsByRarity.get(selectedRarity);

                Identifier selectedCard = rarityCards.get(
                        random.nextInt(rarityCards.size())
                );

                boolean holo = random.nextDouble() < booster.holoChance();

                ItemStack card = CardFactory.createCard(
                        selectedCard,
                        CardOrigin.BOOSTER_PACK,
                        holo
                );

                rewards.add(card);
            }
        }

        // Reveal lower-rarity cards first and save the rarest for last.
        rewards.sort(Comparator.comparingInt(cardStack -> {
            Identifier cardId = cardStack.get(ModComponents.CARD_ID);

            if (cardId == null) {
                return -1;
            }

            return CardRegistry.get(cardId)
                    .map(definition -> rarityRank(definition.rarity()))
                    .orElse(-1);
        }));

        return rewards;
    }

    private static String chooseRarity(
            Map<String, List<Identifier>> cardsByRarity,
            Map<String, Integer> rarityWeights,
            String minimumRarity,
            RandomSource random
    ) {
        int minimumRank = rarityRank(minimumRarity);

        if (minimumRank < 0) {
            return null;
        }

        int totalWeight = 0;

        for (String rarity : cardsByRarity.keySet()) {
            if (rarityRank(rarity) >= minimumRank) {
                totalWeight += rarityWeights.getOrDefault(rarity, 0);
            }
        }

        if (totalWeight <= 0) {
            return null;
        }

        int roll = random.nextInt(totalWeight);

        for (String rarity : cardsByRarity.keySet()) {
            if (rarityRank(rarity) < minimumRank) {
                continue;
            }

            int weight = rarityWeights.getOrDefault(rarity, 0);

            if (roll < weight) {
                return rarity;
            }

            roll -= weight;
        }

        return null;
    }

    private static int rarityRank(String rarity) {
        return switch (rarity) {
            case "common" -> 0;
            case "uncommon" -> 1;
            case "rare" -> 2;
            case "epic" -> 3;
            case "legendary" -> 4;
            default -> -1;
        };
    }
}