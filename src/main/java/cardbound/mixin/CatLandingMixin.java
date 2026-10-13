package cardbound.mixin;

import cardbound.card.ability.CardEffects;
import cardbound.card.ability.PhantomDiveManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class CatLandingMixin {

    @Inject(
            method = "hurtServer",
            at = @At("HEAD"),
            cancellable = true
    )
    private void cardbound$preventFallDamage(
            ServerLevel level,
            DamageSource source,
            float damage,
            CallbackInfoReturnable<Boolean> cir
    ) {
        LivingEntity entity = (LivingEntity) (Object) this;

        // Only protect players.
        if (!(entity instanceof Player player)) {
            return;
        }

        // Ignore everything except fall damage.
        if (!source.is(DamageTypes.FALL)) {
            return;
        }

        // Phantom Dive: temporary fall-damage protection.
        if (PhantomDiveManager.isFallProtected(player)) {
            cir.setReturnValue(false);
            return;
        }

        // Nine Lives: fall-damage protection while effect is active.
        if (!player.hasEffect(CardEffects.CAT_LANDING)) {
            return;
        }

        // Completely cancel fall damage.
        cir.setReturnValue(false);
    }
}