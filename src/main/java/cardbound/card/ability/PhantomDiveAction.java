package cardbound.card.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

public record PhantomDiveAction(
        float launchSpeed,
        float upwardBoost,
        float hitRadius,
        int duration,
        float damage,
        float knockback
) implements AbilityAction {

    public static final MapCodec<PhantomDiveAction> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.FLOAT.fieldOf("launch_speed")
                            .forGetter(PhantomDiveAction::launchSpeed),
                    Codec.FLOAT.fieldOf("upward_boost")
                            .forGetter(PhantomDiveAction::upwardBoost),
                    Codec.FLOAT.fieldOf("hit_radius")
                            .forGetter(PhantomDiveAction::hitRadius),
                    Codec.INT.fieldOf("duration")
                            .forGetter(PhantomDiveAction::duration),
                    Codec.FLOAT.fieldOf("damage")
                            .forGetter(PhantomDiveAction::damage),
                    Codec.FLOAT.fieldOf("knockback")
                            .forGetter(PhantomDiveAction::knockback)
            ).apply(instance, PhantomDiveAction::new));

    @Override
    public Identifier type() {
        return AbilityAction.PHANTOM_DIVE;
    }
}