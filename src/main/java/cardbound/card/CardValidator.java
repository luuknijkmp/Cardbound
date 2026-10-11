
package cardbound.card;

import cardbound.ModComponents;
import cardbound.ModItems;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class CardValidator {

    public static boolean isValidCard(ItemStack stack) {
        if (!stack.is(ModItems.CARD)) {
            return false;
        }

        Identifier cardId = stack.get(ModComponents.CARD_ID);

        if (cardId == null) {
            return false;
        }

        return CardRegistry.exists(cardId);
    }
}
