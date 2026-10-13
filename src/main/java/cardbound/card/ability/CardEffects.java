package cardbound.card.ability;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;

public class CardEffects {

    public static final Holder<MobEffect> PROTECTED = Registry.registerForHolder(
            BuiltInRegistries.MOB_EFFECT,
            Identifier.fromNamespaceAndPath("cardbound", "protected"),
            new ProtectedEffect()
    );

    public static final Holder<MobEffect> GUARDIANS_BITE = Registry.registerForHolder(
            BuiltInRegistries.MOB_EFFECT,
            Identifier.fromNamespaceAndPath("cardbound", "guardians_bite"),
            new GuardiansBiteEffect()
    );
    public static final Holder<MobEffect> CAT_LANDING = Registry.registerForHolder(
            BuiltInRegistries.MOB_EFFECT,
            Identifier.fromNamespaceAndPath("cardbound", "cat_landing"),
            new CatLandingEffect()
    );

    public static void initialize() {
        // Ensures all custom effects are registered.
    }
}