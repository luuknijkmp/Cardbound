package cardbound.card.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

public record WitherAuraAction(
        double radius,
        int duration,
        float damage,
        float healPerHit
) implements AbilityAction {

    public static final Identifier ID =
            Identifier.fromNamespaceAndPath("cardbound", "wither_aura");

    public static final MapCodec<WitherAuraAction> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.DOUBLE.fieldOf("radius")
                            .forGetter(WitherAuraAction::radius),
                    Codec.INT.fieldOf("duration")
                            .forGetter(WitherAuraAction::duration),
                    Codec.FLOAT.fieldOf("damage")
                            .forGetter(WitherAuraAction::damage),
                    Codec.FLOAT.fieldOf("heal_per_hit")
                            .forGetter(WitherAuraAction::healPerHit)
            ).apply(instance, WitherAuraAction::new));

    @Override
    public Identifier type() {
        return ID;
    }
}