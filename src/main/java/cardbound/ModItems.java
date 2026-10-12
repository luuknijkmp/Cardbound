
package cardbound;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import cardbound.booster.BoosterPackItem;
import cardbound.card.CardItem;

public class ModItems {

    // The unique registry key for our card item.
    public static final ResourceKey<Item> CARD_KEY =
            ResourceKey.create(
                    Registries.ITEM,
                    Identifier.fromNamespaceAndPath("cardbound", "card")
            );

    public static final ResourceKey<Item> BOOSTER_PACK_KEY =
            ResourceKey.create(
                    Registries.ITEM,
                    Identifier.fromNamespaceAndPath("cardbound", "booster_pack")
            );

    public static final ResourceKey<Item> VIAL_OF_ESSENCE_KEY =
            ResourceKey.create(
                    Registries.ITEM,
                    Identifier.fromNamespaceAndPath("cardbound", "vial_of_essence")
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

    public static final Item VIAL_OF_ESSENCE = Registry.register(
            BuiltInRegistries.ITEM,
            VIAL_OF_ESSENCE_KEY,
            new Item(new Item.Properties()
                    .setId(VIAL_OF_ESSENCE_KEY)
                    .stacksTo(64))
    );

    public static void initialize() {
        // Calling this method triggers static initialization.
    }
}
