package mctcg;

import mctcg.booster.BoosterPackItemFactory;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import mctcg.booster.BoosterRegistry;

public class ModCreativeTabs {

    private static final Identifier ORIGINS_BOOSTER =
            Identifier.fromNamespaceAndPath(
                    "mctcg",
                    "origins_of_the_realm"
            );

    public static final ResourceKey<CreativeModeTab> MCTCG_TAB_KEY =
            ResourceKey.create(
                    Registries.CREATIVE_MODE_TAB,
                    Identifier.fromNamespaceAndPath("mctcg", "mctcg")
            );

    public static final CreativeModeTab MCTCG_TAB = Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            MCTCG_TAB_KEY,
            CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                    .title(Component.literal("McTCG"))
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
                    })
                    .build()
    );

    public static void initialize() {
        // Triggers static registration.
    }
}