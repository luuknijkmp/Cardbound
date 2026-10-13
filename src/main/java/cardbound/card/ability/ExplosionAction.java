package cardbound.card.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

public record ExplosionAction(
        float radius,
        float damage,
        boolean damagesTerrain
) implements AbilityAction {

    public static final MapCodec<ExplosionAction> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.FLOAT.fieldOf("radius")
                            .forGetter(ExplosionAction::radius),

                    Codec.FLOAT.fieldOf("damage")
                            .forGetter(ExplosionAction::damage),

                    Codec.BOOL.optionalFieldOf("damages_terrain", false)
                            .forGetter(ExplosionAction::damagesTerrain)

            ).apply(instance, ExplosionAction::new));

    @Override
    public Identifier type() {
        return EXPLOSION;
    }
}