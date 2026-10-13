package cardbound.card.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

public record MagmaEruptionAction(
        float radius,
        int duration,
        float damage,
        int damageInterval
) implements AbilityAction {

    public static final MapCodec<MagmaEruptionAction> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.FLOAT.fieldOf("radius")
                            .forGetter(MagmaEruptionAction::radius),

                    Codec.INT.fieldOf("duration")
                            .forGetter(MagmaEruptionAction::duration),

                    Codec.FLOAT.fieldOf("damage")
                            .forGetter(MagmaEruptionAction::damage),

                    Codec.INT.fieldOf("damage_interval")
                            .forGetter(MagmaEruptionAction::damageInterval)
            ).apply(instance, MagmaEruptionAction::new));

    @Override
    public Identifier type() {
        return AbilityAction.MAGMA_ERUPTION;
    }
}