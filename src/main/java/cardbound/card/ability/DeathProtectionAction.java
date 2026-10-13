package cardbound.card.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

public record DeathProtectionAction(int duration) implements AbilityAction {

    public static final MapCodec<DeathProtectionAction> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    com.mojang.serialization.Codec.INT
                            .fieldOf("duration")
                            .forGetter(DeathProtectionAction::duration)
            ).apply(instance, DeathProtectionAction::new));

    @Override
    public Identifier type() {
        return AbilityAction.DEATH_PROTECTION;
    }
}