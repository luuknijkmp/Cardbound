package cardbound.card.ability;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;

public sealed interface AbilityAction
        permits ApplyEffectAction, HealAction, ExplosionAction,
        LightningAction, SummonAction, DeathProtectionAction,
        TeleportAction, DamageAction, WebTrapAction,
        FireballVolleyAction, FireRingAction, WindBurstAction,
        WindLaunchAction, ItemMagnetAction, HealingAuraAction,
        AreaPoisonAction, DragonBreathAction, AreaGlowingAction,
        SandstormCurseAction, LlamaSpitAction, MagmaEruptionAction,
        PhantomDiveAction, SnowballVolleyAction
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

    Identifier ITEM_MAGNET =
            Identifier.fromNamespaceAndPath("cardbound", "item_magnet");

    Identifier HEALING_AURA =
            Identifier.fromNamespaceAndPath("cardbound", "healing_aura");

    Identifier AREA_POISON =
            Identifier.fromNamespaceAndPath("cardbound", "area_poison");

    Identifier DRAGON_BREATH =
            Identifier.fromNamespaceAndPath("cardbound", "dragon_breath");

    Identifier AREA_GLOWING =
            Identifier.fromNamespaceAndPath("cardbound", "area_glowing");

    Identifier SANDSTORM_CURSE =
            Identifier.fromNamespaceAndPath("cardbound", "sandstorm_curse");

    Identifier LLAMA_SPIT =
            Identifier.fromNamespaceAndPath("cardbound", "llama_spit");

    Identifier MAGMA_ERUPTION =
            Identifier.fromNamespaceAndPath("cardbound", "magma_eruption");

    Identifier PHANTOM_DIVE =
            Identifier.fromNamespaceAndPath("cardbound", "phantom_dive");

    Identifier SNOWBALL_VOLLEY =
            Identifier.fromNamespaceAndPath("cardbound", "snowball_volley");


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

                if (type.equals(ITEM_MAGNET)) {
                    return ItemMagnetAction.CODEC;
                }

                if (type.equals(HEALING_AURA)) {
                    return HealingAuraAction.CODEC;
                }

                if (type.equals(AREA_POISON)) {
                    return AreaPoisonAction.CODEC;
                }

                if (type.equals(DRAGON_BREATH)) {
                    return DragonBreathAction.CODEC;
                }

                if (type.equals(AREA_GLOWING)) {
                    return AreaGlowingAction.CODEC;
                }

                if (type.equals(SANDSTORM_CURSE)) {
                    return SandstormCurseAction.CODEC;
                }

                if (type.equals(LLAMA_SPIT)) {
                    return LlamaSpitAction.CODEC;
                }

                if (type.equals(MAGMA_ERUPTION)) {
                    return MagmaEruptionAction.CODEC;
                }

                if (type.equals(PHANTOM_DIVE)) {
                    return PhantomDiveAction.CODEC;
                }

                if (type.equals(SNOWBALL_VOLLEY)) {
                    return SnowballVolleyAction.CODEC;
                }

                throw new IllegalArgumentException(
                        "Unknown ability action type: " + type
                );
            }
    );

    Identifier type();
}