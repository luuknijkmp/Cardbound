package cardbound.card.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

public record AreaPoisonAction(
        float radius,
        int duration,
        int amplifier,
        float damage
) implements AbilityAction {

    public static final MapCodec<AreaPoisonAction> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.FLOAT.fieldOf("radius")
                            .forGetter(AreaPoisonAction::radius),
                    Codec.INT.fieldOf("duration")
                            .forGetter(AreaPoisonAction::duration),
                    Codec.INT.fieldOf("amplifier")
                            .forGetter(AreaPoisonAction::amplifier),
                    Codec.FLOAT.optionalFieldOf("damage", 0.0f)
                            .forGetter(AreaPoisonAction::damage)
            ).apply(instance, AreaPoisonAction::new));

    @Override
    public Identifier type() {
        return AbilityAction.AREA_POISON;
    }
}