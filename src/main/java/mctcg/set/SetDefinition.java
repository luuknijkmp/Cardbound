package mctcg.set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record SetDefinition(
        String name,
        String description
) {
    public static final Codec<SetDefinition> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.STRING.fieldOf("name")
                            .forGetter(SetDefinition::name),

                    Codec.STRING.fieldOf("description")
                            .forGetter(SetDefinition::description)

            ).apply(instance, SetDefinition::new));
}