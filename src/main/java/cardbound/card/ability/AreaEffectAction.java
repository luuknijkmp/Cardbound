package cardbound.card.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

public record AreaEffectAction(
        Identifier effect,
        double radius,
        int duration,
        int amplifier
) implements AbilityAction {

    public static final MapCodec<AreaEffectAction> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Identifier.CODEC.fieldOf("effect")
                            .forGetter(AreaEffectAction::effect),
                    Codec.DOUBLE.fieldOf("radius")
                            .forGetter(AreaEffectAction::radius),
                    Codec.INT.fieldOf("duration")
                            .forGetter(AreaEffectAction::duration),
                    Codec.INT.fieldOf("amplifier")
                            .forGetter(AreaEffectAction::amplifier)
            ).apply(instance, AreaEffectAction::new));

    @Override
    public Identifier type() {
        return AbilityAction.AREA_EFFECT;
    }
}