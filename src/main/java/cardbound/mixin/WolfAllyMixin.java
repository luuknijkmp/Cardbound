package cardbound.mixin;

import cardbound.card.ability.SummonedAlly;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.wolf.Wolf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(Wolf.class)
public abstract class WolfAllyMixin implements SummonedAlly {

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
                    self.getY() + 0.6,
                    self.getZ(),
                    25,
                    0.4,
                    0.5,
                    0.4,
                    0.05
            );

            self.discard();
        }
    }
}