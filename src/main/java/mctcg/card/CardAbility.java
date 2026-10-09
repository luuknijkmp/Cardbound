package mctcg.card;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

public record CardAbility(
        Identifier type,
        Identifier effect,
        int duration,
        int amplifier
) {
    public static final Codec<CardAbility> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Identifier.CODEC.fieldOf("type")
                            .forGetter(CardAbility::type),

                    Identifier.CODEC.fieldOf("effect")
                            .forGetter(CardAbility::effect),

                    Codec.INT.fieldOf("duration")
                            .forGetter(CardAbility::duration),

                    Codec.INT.fieldOf("amplifier")
                            .forGetter(CardAbility::amplifier)

            ).apply(instance, CardAbility::new));
}