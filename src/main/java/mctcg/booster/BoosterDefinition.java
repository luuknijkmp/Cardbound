
package mctcg.booster;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

public record BoosterDefinition(
        String name,
        Identifier set,
        int cardsPerPack
) {
    public static final Codec<BoosterDefinition> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.STRING.fieldOf("name")
                            .forGetter(BoosterDefinition::name),

                    Identifier.CODEC.fieldOf("set")
                            .forGetter(BoosterDefinition::set),

                    Codec.intRange(1, 64).fieldOf("cards_per_pack")
                            .forGetter(BoosterDefinition::cardsPerPack)

            ).apply(instance, BoosterDefinition::new));
}
