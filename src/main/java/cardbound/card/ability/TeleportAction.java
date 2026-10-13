package cardbound.card.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

public record TeleportAction(
        TeleportMode mode,
        float distance,
        boolean throughWalls
) implements AbilityAction {

    public static final MapCodec<TeleportAction> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    TeleportMode.CODEC
                            .fieldOf("mode")
                            .forGetter(TeleportAction::mode),

                    Codec.FLOAT
                            .fieldOf("distance")
                            .forGetter(TeleportAction::distance),

                    Codec.BOOL
                            .optionalFieldOf("through_walls", false)
                            .forGetter(TeleportAction::throughWalls)

            ).apply(instance, TeleportAction::new));

    @Override
    public Identifier type() {
        return AbilityAction.TELEPORT;
    }
}