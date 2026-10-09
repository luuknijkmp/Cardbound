
package mctcg.card;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import java.util.Optional;

public record CardDefinition(
        String name,
        String rarity,
        Identifier set,
        int number,
        Identifier entity,
        int cost,
        Optional<CardAbility> ability
) {

    public static final Codec<CardDefinition> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.STRING.fieldOf("name")
                            .forGetter(CardDefinition::name),

                    Codec.STRING.fieldOf("rarity")
                            .forGetter(CardDefinition::rarity),

                    Identifier.CODEC.fieldOf("set").forGetter(CardDefinition::set),

                    Codec.INT.fieldOf("number")
                            .forGetter(CardDefinition::number),

                    Identifier.CODEC.fieldOf("entity")
                            .forGetter(CardDefinition::entity),

                    Codec.INT.optionalFieldOf("cost", 0)
                            .forGetter(CardDefinition::cost),

                    CardAbility.CODEC.optionalFieldOf("ability")
                            .forGetter(CardDefinition::ability)

            ).apply(instance, CardDefinition::new));
}
