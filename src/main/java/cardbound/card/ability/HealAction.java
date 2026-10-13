package cardbound.card.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

public record HealAction(float amount) implements AbilityAction {

    public static final MapCodec<HealAction> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.FLOAT.fieldOf("amount")
                            .forGetter(HealAction::amount)
            ).apply(instance, HealAction::new));

    @Override
    public Identifier type() {
        return HEAL;
    }
}