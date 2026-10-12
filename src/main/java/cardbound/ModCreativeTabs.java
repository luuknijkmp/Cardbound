package cardbound;

import cardbound.booster.BoosterPackItemFactory;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import cardbound.booster.BoosterRegistry;

public class ModCreativeTabs {

    private static final Identifier ORIGINS_BOOSTER =
            Identifier.fromNamespaceAndPath(
                    "cardbound",
                    "origins_of_the_realm"
            );

    public static final ResourceKey<CreativeModeTab> CARDBOUND_TAB_KEY =
            ResourceKey.create(
                    Registries.CREATIVE_MODE_TAB,
                    Identifier.fromNamespaceAndPath("cardbound", "cardbound")
            );

    public static final CreativeModeTab CARDBOUND_TAB = Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            CARDBOUND_TAB_KEY,
            CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                    .title(Component.literal("Cardbound"))
                    .icon(() -> new ItemStack(ModItems.BOOSTER_PACK))
                    .displayItems((parameters, output) -> {
                        for (Identifier boosterId : BoosterRegistry.getAllIds()
                                .stream()
                                .sorted()
                                .toList()) {

                            ItemStack booster =
                                    BoosterPackItemFactory.createPack(boosterId);

                            if (!booster.isEmpty()) {
                                output.accept(booster);
                            }
                        }
                        output.accept(ModItems.VIAL_OF_ESSENCE);
                    })
                    .build()
    );

    public static void initialize() {
        // Triggers static registration.
    }
}