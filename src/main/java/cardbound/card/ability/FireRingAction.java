package cardbound.card.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

public record FireRingAction(
        float radius,
        int duration,
        float damage
) implements AbilityAction {

    public static final MapCodec<FireRingAction> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.FLOAT.fieldOf("radius")
                            .forGetter(FireRingAction::radius),

                    Codec.INT.fieldOf("duration")
                            .forGetter(FireRingAction::duration),

                    Codec.FLOAT.fieldOf("damage")
                            .forGetter(FireRingAction::damage)
            ).apply(instance, FireRingAction::new));

    @Override
    public Identifier type() {
        return AbilityAction.FIRE_RING;
    }
}