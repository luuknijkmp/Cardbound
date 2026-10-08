
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
        ItemStack stack = new ItemStack(ModItems.CARD);
        stack.set(ModComponents.CARD_ID, cardId);
        stack.set(ModComponents.CARD_ORIGIN, origin.name());

        CardRegistry.get(cardId).ifPresent(definition -> {
            String setName = SetRegistry.get(definition.set())
                    .map(set -> set.name())
                    .orElse(definition.set().toString());

            stack.set(
                    DataComponents.CUSTOM_NAME,
                    Component.literal(definition.name() + " Card").withStyle(style -> style.withItalic(false))
            );


            stack.set(
                    DataComponents.LORE,
                    new ItemLore(List.of(
                            Component.literal("Rarity: ")
                                    .withStyle(ChatFormatting.GRAY)
                                    .append(
                                            Component.literal(
                                                            definition.rarity().substring(0, 1).toUpperCase()
                                                                    + definition.rarity().substring(1).toLowerCase()
                                                    )
                                                    .withStyle(getRarityColor(definition.rarity()))
                                    )
                                    .withStyle(style -> style.withItalic(false)),

                            Component.literal("Set: ")
                                    .withStyle(ChatFormatting.GRAY)
                                    .append(
                                            Component.literal(setName)
                                                    .withStyle(ChatFormatting.WHITE)
                                    )
                                    .withStyle(style -> style.withItalic(false)),

                            Component.literal("#: ")
                                    .withStyle(ChatFormatting.GRAY)
                                    .append(
                                            Component.literal(
                                                    String.format("%03d", definition.number())
                                            ).withStyle(ChatFormatting.WHITE)
                                    )
                                    .withStyle(style -> style.withItalic(false)),
                            Component.literal("Origin: ")
                                    .withStyle(ChatFormatting.GRAY)
                                    .append(
                                            Component.literal(origin.getDisplayName())
                                                    .withStyle(origin.getColor())
                                    )
                                    .withStyle(style -> style.withItalic(false))
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

