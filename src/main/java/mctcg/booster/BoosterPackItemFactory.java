package mctcg.booster;

import mctcg.ModComponents;
import mctcg.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

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

        return stack;
    }
}