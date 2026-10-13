package cardbound.card.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

public record HealingAuraAction(
        float radius,
        int duration,
        float healing,
        int interval
) implements AbilityAction {

    public static final MapCodec<HealingAuraAction> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.FLOAT.fieldOf("radius")
                            .forGetter(HealingAuraAction::radius),

                    Codec.INT.fieldOf("duration")
                            .forGetter(HealingAuraAction::duration),

                    Codec.FLOAT.fieldOf("healing")
                            .forGetter(HealingAuraAction::healing),

                    Codec.INT.fieldOf("interval")
                            .forGetter(HealingAuraAction::interval)
            ).apply(instance, HealingAuraAction::new));

    @Override
    public Identifier type() {
        return AbilityAction.HEALING_AURA;
    }
}