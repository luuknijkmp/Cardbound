package cardbound.card.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

public record OpenMenuAction(
        String menu
) implements AbilityAction {

    public static final Identifier ID =
            Identifier.fromNamespaceAndPath("cardbound", "open_menu");

    public static final MapCodec<OpenMenuAction> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.STRING.fieldOf("menu")
                            .forGetter(OpenMenuAction::menu)
            ).apply(instance, OpenMenuAction::new));

    @Override
    public Identifier type() {
        return ID;
    }
}