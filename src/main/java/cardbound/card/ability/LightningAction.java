package cardbound.card.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

public record LightningAction(
        AbilityTarget target,
        float range
) implements AbilityAction {

    public static final MapCodec<LightningAction> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    AbilityTarget.CODEC.optionalFieldOf(
                            "target", AbilityTarget.LOOKED_AT
                    ).forGetter(LightningAction::target),

                    Codec.FLOAT.optionalFieldOf("range", 20.0F)
                            .forGetter(LightningAction::range)

            ).apply(instance, LightningAction::new));

    @Override
    public Identifier type() {
        return LIGHTNING;
    }
}