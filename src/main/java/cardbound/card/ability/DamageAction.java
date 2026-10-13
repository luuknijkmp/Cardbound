package cardbound.card.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

public record DamageAction(
        float amount,
        AbilityTarget target,
        float range
) implements AbilityAction {

    public static final MapCodec<DamageAction> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.FLOAT.fieldOf("amount")
                            .forGetter(DamageAction::amount),

                    AbilityTarget.CODEC.fieldOf("target")
                            .forGetter(DamageAction::target),

                    Codec.FLOAT.fieldOf("range")
                            .forGetter(DamageAction::range)
            ).apply(instance, DamageAction::new));

    @Override
    public Identifier type() {
        return AbilityAction.DAMAGE;
    }
}