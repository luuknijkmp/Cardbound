package cardbound.booster;

import cardbound.ModComponents;
import cardbound.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.component.TooltipDisplay;

public class BoosterPackItemFactory {

    public static ItemStack createPack(Identifier boosterId) {

        // Don't create a pack for an unknown booster definition.
        var definitionOptional = BoosterRegistry.get(boosterId);

        if (definitionOptional.isEmpty()) {
            return ItemStack.EMPTY;
        }

        BoosterDefinition definition = definitionOptional.get();

        // Create an instance of our registered booster item.
        ItemStack stack = new ItemStack(ModItems.BOOSTER_PACK);

        // Store which booster definition this instance uses.
        stack.set(ModComponents.PACK_ID, boosterId);

        // Display the friendly name from the JSON definition.
        stack.set(
                DataComponents.CUSTOM_NAME,
                Component.literal(definition.name())
                        .withStyle(style -> style.withItalic(false))
        );

        int maxCards = definition.slots()
                .stream()
                .mapToInt(BoosterSlot::count)
                .sum();

        stack.set(
                DataComponents.LORE,
                new ItemLore(List.of(
                        Component.literal("Cards: ")
                                .withStyle(ChatFormatting.GRAY)
                                .append(
                                        Component.literal(maxCards + "/" + maxCards)
                                                .withStyle(ChatFormatting.WHITE)
                                )
                                .withStyle(style -> style.withItalic(false))
                ))
        );

        // Hide the cards stored inside the booster from its tooltip.
        stack.set(
                DataComponents.TOOLTIP_DISPLAY,
                TooltipDisplay.DEFAULT.withHidden(DataComponents.CONTAINER, true)
        );

        return stack;
    }
}