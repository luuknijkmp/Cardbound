package mctcg.booster;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record BoosterSlot(
        int count,
        String minimumRarity
) {
    public static final Codec<BoosterSlot> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.intRange(1, 64)
                            .fieldOf("count")
                            .forGetter(BoosterSlot::count),

                    Codec.STRING
                            .fieldOf("minimum_rarity")
                            .forGetter(BoosterSlot::minimumRarity)
            ).apply(instance, BoosterSlot::new));
}