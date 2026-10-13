package cardbound.mixin;

import cardbound.card.ability.SilverfishAllyTargetGoal;
import cardbound.card.ability.SummonedAlly;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Silverfish;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(Silverfish.class)
public abstract class SilverfishAllyMixin implements SummonedAlly {

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
                    self.getY() + 0.3,
                    self.getZ(),
                    15,
                    0.3,
                    0.3,
                    0.3,
                    0.03
            );

            self.discard();
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void cardbound$protectOwner(CallbackInfo ci) {
        if (cardbound$ownerUuid == null) {
            return;
        }

        Mob silverfish = (Mob) (Object) this;
        LivingEntity target = silverfish.getTarget();

        if (target == null) {
            return;
        }

        if (target.getUUID().equals(cardbound$ownerUuid)
                || (target instanceof SummonedAlly ally
                && ally.cardbound$getOwnerUuid() != null)) {
            silverfish.setTarget(null);
        }
    }

    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void cardbound$registerAllyTargetGoal(CallbackInfo ci) {
        Silverfish silverfish = (Silverfish) (Object) this;

        ((MobAccessor) silverfish).cardbound$getTargetSelector().addGoal(
                1,
                new SilverfishAllyTargetGoal(silverfish) {
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