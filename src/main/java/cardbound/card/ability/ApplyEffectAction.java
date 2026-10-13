package cardbound.card.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

public record ApplyEffectAction(
        Identifier effect,
        int duration,
        int amplifier,
        AbilityTarget target,
        float range
) implements AbilityAction {

    public static final MapCodec<ApplyEffectAction> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Identifier.CODEC.fieldOf("effect")
                            .forGetter(ApplyEffectAction::effect),

                    Codec.INT.fieldOf("duration")
                            .forGetter(ApplyEffectAction::duration),

                    Codec.INT.fieldOf("amplifier")
                            .forGetter(ApplyEffectAction::amplifier),

                    AbilityTarget.CODEC.optionalFieldOf(
                            "target", AbilityTarget.SELF
                    ).forGetter(ApplyEffectAction::target),

                    Codec.FLOAT.optionalFieldOf("range", 20.0F)
                            .forGetter(ApplyEffectAction::range)

            ).apply(instance, ApplyEffectAction::new));

    @Override
    public Identifier type() {
        return APPLY_EFFECT;
    }
}