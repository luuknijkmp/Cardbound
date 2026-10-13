package cardbound.card.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

public record AreaGlowingAction(
        float radius,
        int duration
) implements AbilityAction {

    public static final MapCodec<AreaGlowingAction> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    com.mojang.serialization.Codec.FLOAT
                            .fieldOf("radius")
                            .forGetter(AreaGlowingAction::radius),

                    com.mojang.serialization.Codec.INT
                            .fieldOf("duration")
                            .forGetter(AreaGlowingAction::duration)
            ).apply(instance, AreaGlowingAction::new));

    @Override
    public Identifier type() {
        return AbilityAction.AREA_GLOWING;
    }
}