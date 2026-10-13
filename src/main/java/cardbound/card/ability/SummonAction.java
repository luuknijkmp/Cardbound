package cardbound.card.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

public record SummonAction(
        Identifier entity,
        int count,
        int duration
) implements AbilityAction {

    public static final MapCodec<SummonAction> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Identifier.CODEC.fieldOf("entity")
                            .forGetter(SummonAction::entity),

                    Codec.INT.fieldOf("count")
                            .forGetter(SummonAction::count),

                    Codec.INT.fieldOf("duration")
                            .forGetter(SummonAction::duration)

            ).apply(instance, SummonAction::new));

    @Override
    public Identifier type() {
        return SUMMON;
    }
}