package cardbound.card.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

public record DragonBreathAction(
        float range,
        float radius,
        int duration,
        float damage,
        int damageInterval
) implements AbilityAction {

    public static final MapCodec<DragonBreathAction> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(

                    Codec.FLOAT.fieldOf("range")
                            .forGetter(DragonBreathAction::range),

                    Codec.FLOAT.fieldOf("radius")
                            .forGetter(DragonBreathAction::radius),

                    Codec.INT.fieldOf("duration")
                            .forGetter(DragonBreathAction::duration),

                    Codec.FLOAT.fieldOf("damage")
                            .forGetter(DragonBreathAction::damage),

                    Codec.INT.fieldOf("damage_interval")
                            .forGetter(DragonBreathAction::damageInterval)

            ).apply(instance, DragonBreathAction::new));

    @Override
    public Identifier type() {
        return AbilityAction.DRAGON_BREATH;
    }
}