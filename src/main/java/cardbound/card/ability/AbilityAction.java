package cardbound.card.ability;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;

public sealed interface AbilityAction
        permits ApplyEffectAction, HealAction, ExplosionAction,
        LightningAction, SummonAction, DeathProtectionAction,
        TeleportAction, DamageAction, WebTrapAction,
        FireballVolleyAction, FireRingAction, WindBurstAction,
        WindLaunchAction
    {

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

    Identifier TELEPORT =
            Identifier.fromNamespaceAndPath("cardbound", "teleport");

    Identifier DAMAGE =
            Identifier.fromNamespaceAndPath("cardbound", "damage");

    Identifier WEB_TRAP =
            Identifier.fromNamespaceAndPath("cardbound", "web_trap");

    Identifier FIREBALL_VOLLEY =
            Identifier.fromNamespaceAndPath("cardbound", "fireball_volley");

    Identifier FIRE_RING =
            Identifier.fromNamespaceAndPath("cardbound", "fire_ring");

    Identifier WIND_BURST =
            Identifier.fromNamespaceAndPath("cardbound", "wind_burst");

    Identifier WIND_LAUNCH =
            Identifier.fromNamespaceAndPath("cardbound", "wind_launch");

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

                if (type.equals(TELEPORT)) {
                    return TeleportAction.CODEC;
                }

                if (type.equals(DAMAGE)) {
                    return DamageAction.CODEC;
                }

                if (type.equals(WEB_TRAP)) {
                    return WebTrapAction.CODEC;
                }

                if (type.equals(FIREBALL_VOLLEY)) {
                    return FireballVolleyAction.CODEC;
                }

                if (type.equals(FIRE_RING)) {
                    return FireRingAction.CODEC;
                }

                if (type.equals(WIND_BURST)) {
                    return WindBurstAction.CODEC;
                }

                if (type.equals(WIND_LAUNCH)) {
                    return WindLaunchAction.CODEC;
                }

                throw new IllegalArgumentException(
                        "Unknown ability action type: " + type
                );
            }
    );

    Identifier type();
}