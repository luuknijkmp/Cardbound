
package mctcg.card;

import mctcg.ModComponents;
import mctcg.ModItems;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;

import java.util.List;

import net.minecraft.world.item.component.ItemLore;

import net.minecraft.ChatFormatting;

import mctcg.set.SetRegistry;

public class CardFactory {


    public static ItemStack createCard(Identifier cardId, CardOrigin origin) {
        return createCard(cardId, origin, false);
    }

    public static ItemStack createCard(
            Identifier cardId,
            CardOrigin origin,
            boolean holo
    ) {
        ItemStack stack = new ItemStack(ModItems.CARD);
        stack.set(ModComponents.CARD_ID, cardId);
        stack.set(ModComponents.CARD_ORIGIN, origin.name());
        stack.set(ModComponents.CARD_HOLO, holo);

        if (holo) {
            stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        }

        CardRegistry.get(cardId).ifPresent(definition -> {
            // Use a custom item model if the card defines one.
            // Otherwise, keep the default mctcg:card model.
            definition.texture().ifPresent(textureId ->
                    stack.set(DataComponents.ITEM_MODEL, textureId)
            );

            String setName = SetRegistry.get(definition.set())
                    .map(set -> set.name())
                    .orElse(definition.set().toString());

            stack.set(
                    DataComponents.CUSTOM_NAME,
                    Component.literal(definition.name() + " Card")
                            .withStyle(getRarityColor(definition.rarity()))
                            .withStyle(style -> style.withItalic(false))
            );

            // Line 1: Rarity [Holo] | Set | Number | Origin
            Component infoLine = Component.literal(
                            definition.rarity().substring(0, 1).toUpperCase()
                                    + definition.rarity().substring(1).toLowerCase()
                    )
                    .withStyle(getRarityColor(definition.rarity()));

            if (holo) {
                infoLine = infoLine.copy().append(
                        Component.literal(" [Holo]")
                                .withStyle(ChatFormatting.WHITE)
                );
            }

            infoLine = infoLine.copy()
                    .append(Component.literal(" | ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(setName).withStyle(ChatFormatting.WHITE))
                    .append(Component.literal(" | ").withStyle(ChatFormatting.GRAY))
                    .append(
                            Component.literal(String.format("%03d", definition.number()))
                                    .withStyle(ChatFormatting.WHITE)
                    )
                    .append(Component.literal(" | ").withStyle(ChatFormatting.GRAY))
                    .append(
                            Component.literal(origin.getDisplayName())
                                    .withStyle(origin.getColor())
                    );

// Line 2: Ability description
            Component abilityLine = Component.literal("Ability: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(
                            Component.literal(
                                            definition.abilityDescription().isBlank()
                                                    ? "None"
                                                    : definition.abilityDescription()
                                    )
                                    .withStyle(ChatFormatting.WHITE)
                    );

// Line 3: Essence cost
            Component costLine = Component.literal("Cost: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(
                            Component.literal(String.valueOf(definition.cost()))
                                    .withStyle(ChatFormatting.WHITE)
                    );

// Disable Minecraft's default italic formatting on all three lines.
            stack.set(
                    DataComponents.LORE,
                    new ItemLore(List.of(
                            infoLine.copy().withStyle(style -> style.withItalic(false)),
                            abilityLine.copy().withStyle(style -> style.withItalic(false)),
                            costLine.copy().withStyle(style -> style.withItalic(false))
                    ))
            );

        });

        return stack;
    }


    private static ChatFormatting getRarityColor(String rarity) {
        return switch (rarity.toLowerCase()) {
            case "common" -> ChatFormatting.WHITE;
            case "uncommon" -> ChatFormatting.GREEN;
            case "rare" -> ChatFormatting.AQUA;
            case "epic" -> ChatFormatting.LIGHT_PURPLE;
            case "legendary" -> ChatFormatting.GOLD;
            default -> ChatFormatting.GRAY;
        };
    }
}

