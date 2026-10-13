package cardbound.card.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

public record WindBurstAction(
        float radius,
        float strength
) implements AbilityAction {

    public static final MapCodec<WindBurstAction> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.FLOAT.fieldOf("radius")
                            .forGetter(WindBurstAction::radius),

                    Codec.FLOAT.fieldOf("strength")
                            .forGetter(WindBurstAction::strength)
            ).apply(instance, WindBurstAction::new));

    @Override
    public Identifier type() {
        return AbilityAction.WIND_BURST;
    }
}