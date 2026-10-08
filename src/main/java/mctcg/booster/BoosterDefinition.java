
package mctcg.booster;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Map;

public record BoosterDefinition(
        String name,
        Identifier set,
        Map<String, Integer> rarityWeights,
        double holoChance,
        List<BoosterSlot> slots
) {
    public static final Codec<BoosterDefinition> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.STRING.fieldOf("name")
                            .forGetter(BoosterDefinition::name),

                    Identifier.CODEC.fieldOf("set")
                            .forGetter(BoosterDefinition::set),

                    Codec.unboundedMap(Codec.STRING, Codec.intRange(1, 10000))
                            .fieldOf("rarity_weights")
                            .forGetter(BoosterDefinition::rarityWeights),

                    Codec.doubleRange(0.0, 1.0)
                            .fieldOf("holo_chance")
                            .forGetter(BoosterDefinition::holoChance),

                    BoosterSlot.CODEC.listOf()
                            .fieldOf("slots")
                            .forGetter(BoosterDefinition::slots)

            ).apply(instance, BoosterDefinition::new));

}
