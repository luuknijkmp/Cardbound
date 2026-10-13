package cardbound.card.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

public record WebTrapAction(
        int duration,
        float range
) implements AbilityAction {

    public static final MapCodec<WebTrapAction> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    com.mojang.serialization.Codec.INT
                            .fieldOf("duration")
                            .forGetter(WebTrapAction::duration),

                    com.mojang.serialization.Codec.FLOAT
                            .fieldOf("range")
                            .forGetter(WebTrapAction::range)
            ).apply(instance, WebTrapAction::new));

    @Override
    public Identifier type() {
        return AbilityAction.WEB_TRAP;
    }
}