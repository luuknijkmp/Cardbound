package cardbound.mixin;

import cardbound.card.ability.CardEffects;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ARGB;

@Mixin(LivingEntity.class)
public abstract class ProtectedDeathMixin {

    @Inject(
            method = "checkTotemDeathProtection",
            at = @At("HEAD"),
            cancellable = true
    )
    private void cardbound$protectFromDeath(
            DamageSource killingDamage,
            CallbackInfoReturnable<Boolean> cir
    ) {
        LivingEntity entity = (LivingEntity) (Object) this;

        // Only players can use Last of the Living.
        if (!(entity instanceof Player player)) {
            return;
        }

        // Respect Minecraft's normal death-protection restrictions.
        if (killingDamage.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return;
        }

        // No protection effect? Let vanilla handle Totems normally.
        if (!player.hasEffect(CardEffects.PROTECTED)) {
            return;
        }

        /// Consume the one-time protection.
        player.removeEffect(CardEffects.PROTECTED);

// Survive with exactly half a heart.
        player.setHealth(1.0F);

// Create a magical burst when protection activates.
        if (player.level() instanceof ServerLevel serverLevel) {

            // Bright cyan magical particles.
            serverLevel.sendParticles(
                    ColorParticleOption.create(
                            ParticleTypes.ENTITY_EFFECT,
                            ARGB.color(255, 0x55DDE0)
                    ),
                    player.getX(),
                    player.getY() + 1.0,
                    player.getZ(),
                    35,
                    0.5,
                    0.8,
                    0.5,
                    0.15
            );

            // Blue spectral particles.
            serverLevel.sendParticles(
                    ParticleTypes.SOUL,
                    player.getX(),
                    player.getY() + 1.0,
                    player.getZ(),
                    20,
                    0.4,
                    0.7,
                    0.4,
                    0.08
            );
        }

// Tell Minecraft that death was prevented.
        cir.setReturnValue(true);
    }
}