package cardbound.card.ability;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;

public sealed interface AbilityAction
        permits ApplyEffectAction, HealAction, ExplosionAction,
        LightningAction, SummonAction, DeathProtectionAction {

    Identifier APPLY_EFFECT =
            Identifier.fromNamespaceAndPath("cardbound", "apply_effect");

    Identifier HEAL =
            Identifier.fromNamespaceAndPath("cardbound", "heal");

    Identifier EXPLOSION =
            Identifier.fromNamespaceAndPath("cardbound", "explosion");

    Identifier LIGHTNING =
            Identifier.fromNamespaceAndPath("cardbound", "lightning");

    Identifier SUMMON =
            Identifier.fromNamespaceAndPath("cardbound", "summon");

    Identifier DEATH_PROTECTION =
            Identifier.fromNamespaceAndPath("cardbound", "death_protection");

    Codec<AbilityAction> CODEC = Identifier.CODEC.dispatch(
            "type",
            AbilityAction::type,
            type -> {
                if (type.equals(APPLY_EFFECT)) {
                    return ApplyEffectAction.CODEC;
                }

                if (type.equals(HEAL)) {
                    return HealAction.CODEC;
                }

                if (type.equals(EXPLOSION)) {
                    return ExplosionAction.CODEC;
                }

                if (type.equals(LIGHTNING)) {
                    return LightningAction.CODEC;
                }

                if (type.equals(SUMMON)) {
                    return SummonAction.CODEC;
                }

                if (type.equals(DEATH_PROTECTION)) {
                    return DeathProtectionAction.CODEC;
                }

                throw new IllegalArgumentException(
                        "Unknown ability action type: " + type
                );
            }
    );

    Identifier type();
}