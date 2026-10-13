package cardbound.mixin;

import cardbound.card.ability.AllyTargetGoal;
import cardbound.card.ability.SummonedAlly;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NearestAttackableTargetGoal.class)
public abstract class ZombieTargetGoalMixin {

    @Inject(
            method = "canUse",
            at = @At("HEAD"),
            cancellable = true
    )
    private void cardbound$disableVanillaTargeting(
            CallbackInfoReturnable<Boolean> cir
    ) {
        NearestAttackableTargetGoal<?> goal =
                (NearestAttackableTargetGoal<?>) (Object) this;

        // Allow Cardbound's custom allied targeting goal.
        if (goal instanceof AllyTargetGoal) {
            return;
        }

        // The Mob field is inherited from TargetGoal.
        Mob mob = ((TargetGoalAccessor) goal).cardbound$getMob();

        // Disable vanilla nearest-target goals for summoned allies.
        if (mob instanceof SummonedAlly ally
                && ally.cardbound$getOwnerUuid() != null) {
            cir.setReturnValue(false);
        }
    }
}