package cardbound.mixin;

import cardbound.card.ability.AllyTargetGoal;
import cardbound.card.ability.SummonedAlly;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Vex;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.GoalSelector;

import java.util.UUID;

@Mixin(Vex.class)
public abstract class VexAllyMixin implements SummonedAlly {

    @Unique
    private UUID cardbound$ownerUuid;

    @Unique
    private long cardbound$expirationTick;

    @Override
    public UUID cardbound$getOwnerUuid() {
        return cardbound$ownerUuid;
    }

    @Override
    public void cardbound$setOwnerUuid(UUID ownerUuid) {
        this.cardbound$ownerUuid = ownerUuid;
    }

    @Override
    public long cardbound$getExpirationTick() {
        return cardbound$expirationTick;
    }

    @Override
    public void cardbound$setExpirationTick(long expirationTick) {
        this.cardbound$expirationTick = expirationTick;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void cardbound$tickAlly(CallbackInfo ci) {
        Vex vex = (Vex) (Object) this;
        ((MobAccessor) vex).cardbound$getTargetSelector()
                .removeAllGoals(goal ->
                        goal instanceof net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal
                );

        if (cardbound$ownerUuid == null
                || !(vex.level() instanceof ServerLevel level)) {
            return;
        }

        // Remove summoned vexes after their duration expires.
        if (level.getGameTime() >= cardbound$expirationTick) {
            level.sendParticles(
                    ParticleTypes.SOUL,
                    vex.getX(),
                    vex.getY() + 0.5,
                    vex.getZ(),
                    15,
                    0.3,
                    0.3,
                    0.3,
                    0.03
            );

            vex.discard();
            return;
        }

        // Never target the summoner or any Cardbound summoned ally.
        LivingEntity target = vex.getTarget();

        if (target != null
                && (target.getUUID().equals(cardbound$ownerUuid)
                || (target instanceof SummonedAlly ally
                && ally.cardbound$getOwnerUuid() != null))) {
            vex.setTarget(null);
        }
    }

    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void cardbound$registerAllyGoals(CallbackInfo ci) {
        Vex vex = (Vex) (Object) this;

        ((MobAccessor) vex).cardbound$getTargetSelector().addGoal(
                1,
                new AllyTargetGoal(vex) {

                    @Override
                    public boolean canUse() {
                        return cardbound$ownerUuid != null
                                && super.canUse();
                    }

                    @Override
                    public boolean canContinueToUse() {
                        return cardbound$ownerUuid != null
                                && super.canContinueToUse();
                    }
                }
        );
    }
}