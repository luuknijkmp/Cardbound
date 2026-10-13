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

    public static void initialize() {
        // Calling this method ensures the static registration runs.
    }
}