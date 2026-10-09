
package mctcg;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import mctcg.booster.BoosterPackItem;
import mctcg.card.CardItem;

public class ModItems {

    // The unique registry key for our card item.
    public static final ResourceKey<Item> CARD_KEY =
            ResourceKey.create(
                    Registries.ITEM,
                    Identifier.fromNamespaceAndPath("mctcg", "card")
            );

    public static final ResourceKey<Item> BOOSTER_PACK_KEY =
            ResourceKey.create(
                    Registries.ITEM,
                    Identifier.fromNamespaceAndPath("mctcg", "booster_pack")
            );

    // Create and register our item.
    public static final Item CARD = Registry.register(
            BuiltInRegistries.ITEM,
            CARD_KEY,
            new CardItem(new Item.Properties()
                    .setId(CARD_KEY)
                    .stacksTo(1))
    );

    public static final Item BOOSTER_PACK = Registry.register(
            BuiltInRegistries.ITEM,
            BOOSTER_PACK_KEY,
            new BoosterPackItem(new Item.Properties()
                    .setId(BOOSTER_PACK_KEY)
                    .stacksTo(1))
    );

    public static void initialize() {
        // Calling this method triggers static initialization.
    }
}
