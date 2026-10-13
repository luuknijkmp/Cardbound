package cardbound.mixin;

import cardbound.card.ability.SummonedAlly;
import net.minecraft.world.entity.monster.zombie.Zombie;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import cardbound.card.ability.AllyTargetGoal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import org.spongepowered.asm.mixin.Shadow;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.core.particles.ParticleTypes;

import java.util.UUID;

@Mixin(Zombie.class)
public abstract class ZombieAllyMixin implements SummonedAlly {

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
    private void cardbound$expireSummon(CallbackInfo ci) {
        if (cardbound$ownerUuid == null) {
            return;
        }

        Entity self = (Entity) (Object) this;

        if (!(self.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (serverLevel.getGameTime() >= cardbound$expirationTick) {

            serverLevel.sendParticles(
                    ParticleTypes.SOUL,
                    self.getX(),
                    self.getY() + 1.0,
                    self.getZ(),
                    25,
                    0.4,
                    0.7,
                    0.4,
                    0.05
            );

            self.discard();
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void cardbound$protectOwner(CallbackInfo ci) {
        if (cardbound$ownerUuid == null) {
            return;
        }

        Mob zombie = (Mob) (Object) this;
        LivingEntity target = zombie.getTarget();

        if (target != null
                && target.getUUID().equals(cardbound$ownerUuid)) {
            zombie.setTarget(null);
        }
    }

    @Inject(method = "addBehaviourGoals", at = @At("TAIL"))
    private void cardbound$registerAllyTargetGoal(CallbackInfo ci) {
        Zombie zombie = (Zombie) (Object) this;

        ((MobAccessor) zombie).cardbound$getTargetSelector().addGoal(
                1,
                new AllyTargetGoal(zombie) {
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