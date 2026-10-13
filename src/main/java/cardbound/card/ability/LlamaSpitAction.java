package cardbound.card.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

public record LlamaSpitAction(
        float range
) implements AbilityAction {

    public static final MapCodec<LlamaSpitAction> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.FLOAT.fieldOf("range")
                            .forGetter(LlamaSpitAction::range)
            ).apply(instance, LlamaSpitAction::new));

    @Override
    public Identifier type() {
        return AbilityAction.LLAMA_SPIT;
    }
}