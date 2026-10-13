package cardbound.card.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

public record ItemMagnetAction(
        float radius
) implements AbilityAction {

    public static final MapCodec<ItemMagnetAction> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.FLOAT.fieldOf("radius")
                            .forGetter(ItemMagnetAction::radius)
            ).apply(instance, ItemMagnetAction::new));

    @Override
    public Identifier type() {
        return AbilityAction.ITEM_MAGNET;
    }
}