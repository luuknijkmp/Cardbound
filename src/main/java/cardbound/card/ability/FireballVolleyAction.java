package cardbound.card.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

public record FireballVolleyAction(
        int count,
        int interval,
        boolean explosive
) implements AbilityAction {

    public static final MapCodec<FireballVolleyAction> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(

                    Codec.INT.fieldOf("count")
                            .forGetter(FireballVolleyAction::count),

                    Codec.INT.fieldOf("interval")
                            .forGetter(FireballVolleyAction::interval),

                    Codec.BOOL.optionalFieldOf("explosive", false)
                            .forGetter(FireballVolleyAction::explosive)

            ).apply(instance, FireballVolleyAction::new));

    @Override
    public Identifier type() {
        return AbilityAction.FIREBALL_VOLLEY;
    }
}