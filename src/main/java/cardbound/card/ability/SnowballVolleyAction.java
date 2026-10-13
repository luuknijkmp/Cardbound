package cardbound.card.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

public record SnowballVolleyAction(
        int count,
        int interval,
        float speed,
        float knockback
) implements AbilityAction {

    public static final MapCodec<SnowballVolleyAction> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.INT.fieldOf("count")
                            .forGetter(SnowballVolleyAction::count),
                    Codec.INT.fieldOf("interval")
                            .forGetter(SnowballVolleyAction::interval),
                    Codec.FLOAT.fieldOf("speed")
                            .forGetter(SnowballVolleyAction::speed),
                    Codec.FLOAT.fieldOf("knockback")
                            .forGetter(SnowballVolleyAction::knockback)
            ).apply(instance, SnowballVolleyAction::new));

    @Override
    public Identifier type() {
        return AbilityAction.SNOWBALL_VOLLEY;
    }
}