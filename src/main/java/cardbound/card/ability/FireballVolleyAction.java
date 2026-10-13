package cardbound.card.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

public record FireballVolleyAction(
        int count,
        int interval
) implements AbilityAction {

    public static final MapCodec<FireballVolleyAction> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    com.mojang.serialization.Codec.INT
                            .fieldOf("count")
                            .forGetter(FireballVolleyAction::count),

                    com.mojang.serialization.Codec.INT
                            .fieldOf("interval")
                            .forGetter(FireballVolleyAction::interval)
            ).apply(instance, FireballVolleyAction::new));

    @Override
    public Identifier type() {
        return AbilityAction.FIREBALL_VOLLEY;
    }
}