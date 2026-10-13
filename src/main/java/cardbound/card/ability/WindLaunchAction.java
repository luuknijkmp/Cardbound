package cardbound.card.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

public record WindLaunchAction(
        float strength
) implements AbilityAction {

    public static final MapCodec<WindLaunchAction> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.FLOAT.fieldOf("strength")
                            .forGetter(WindLaunchAction::strength)
            ).apply(instance, WindLaunchAction::new));

    @Override
    public Identifier type() {
        return AbilityAction.WIND_LAUNCH;
    }
}