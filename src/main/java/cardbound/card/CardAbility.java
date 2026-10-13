package cardbound.card;

import cardbound.card.ability.AbilityAction;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record CardAbility(
        List<AbilityAction> actions
) {

    public static final Codec<CardAbility> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    AbilityAction.CODEC.listOf()
                            .fieldOf("actions")
                            .forGetter(CardAbility::actions)

            ).apply(instance, CardAbility::new));
}