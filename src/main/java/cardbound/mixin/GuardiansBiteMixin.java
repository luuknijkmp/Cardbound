package cardbound.mixin;

import cardbound.card.ability.CardEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class GuardiansBiteMixin {

    @Inject(method = "actuallyHurt", at = @At("TAIL"))
    private void cardbound$guardiansBite(
            ServerLevel level,
            DamageSource source,
            float damage,
            CallbackInfo ci
    ) {

        LivingEntity victim = (LivingEntity) (Object) this;

        if (!(victim instanceof Player player)) {
            return;
        }

        if (!player.hasEffect(CardEffects.GUARDIANS_BITE)) {
            return;
        }

        Entity attacker = source.getEntity();

        if (!(attacker instanceof LivingEntity livingAttacker)) {
            return;
        }

// Don't retaliate against Guardian's Bite's own reflected magic damage.
        if (source.is(net.minecraft.world.damagesource.DamageTypes.INDIRECT_MAGIC)) {
            return;
        }

        if (attacker == victim || !livingAttacker.isAlive()) {
            return;
        }

        // The guardian spirit bites the attacker back.
        livingAttacker.hurt(
                player.damageSources().indirectMagic(player, player),
                3.0F
        );

        // Spectral wolf-bite particles.
        level.sendParticles(
                ParticleTypes.SOUL,
                attacker.getX(),
                attacker.getY() + attacker.getBbHeight() * 0.5,
                attacker.getZ(),
                18,
                0.3,
                0.4,
                0.3,
                0.05
        );
    }
}