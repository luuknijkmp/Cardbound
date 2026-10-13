package cardbound.card.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

public record SandstormCurseAction(
        float radius,
        int duration
) implements AbilityAction {

    public static final MapCodec<SandstormCurseAction> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.FLOAT.fieldOf("radius")
                            .forGetter(SandstormCurseAction::radius),
                    Codec.INT.fieldOf("duration")
                            .forGetter(SandstormCurseAction::duration)
            ).apply(instance, SandstormCurseAction::new));

    @Override
    public Identifier type() {
        return AbilityAction.SANDSTORM_CURSE;
    }
}