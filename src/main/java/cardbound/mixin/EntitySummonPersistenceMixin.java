package cardbound.mixin;

import cardbound.card.ability.SummonedAlly;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntitySummonPersistenceMixin {

    @Inject(
            method = "shouldBeSaved",
            at = @At("HEAD"),
            cancellable = true
    )
    private void cardbound$dontSaveSummonedAllies(
            CallbackInfoReturnable<Boolean> cir
    ) {
        if ((Object) this instanceof SummonedAlly ally
                && ally.cardbound$getOwnerUuid() != null) {
            cir.setReturnValue(false);
        }
    }
}