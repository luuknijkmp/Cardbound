package cardbound.card.ability;

import cardbound.card.CardAbility;
import cardbound.card.CardDefinition;
import cardbound.essence.PlayerEssence;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;

public final class AbilityExecutor {

    private AbilityExecutor() {
    }

    public static boolean activate(Player player, CardDefinition definition) {
        if (definition.ability().isEmpty()) {
            return false;
        }

        CardAbility ability = definition.ability().get();

        if (ability.actions().isEmpty() || definition.cost() < 0) {
            return false;
        }

        int currentEssence = player.getAttachedOrCreate(PlayerEssence.ESSENCE);
        int cost = definition.cost();

        if (!player.isCreative() && currentEssence < cost) {
            return false;
        }

        // Validate every action before executing any of them.
        for (AbilityAction action : ability.actions()) {
            if (action instanceof ApplyEffectAction effectAction) {
                if (effectAction.duration() <= 0
                        || effectAction.amplifier() < 0
                        || effectAction.amplifier() > 255
                        || BuiltInRegistries.MOB_EFFECT.get(effectAction.effect()).isEmpty()) {
                    return false;
                }
            } else if (action instanceof HealAction healAction) {
                if (!Float.isFinite(healAction.amount())
                        || healAction.amount() <= 0) {
                    return false;
                }
            }
            else if (action instanceof ExplosionAction explosionAction) {
                if (!Float.isFinite(explosionAction.radius())
                        || !Float.isFinite(explosionAction.damage())
                        || explosionAction.radius() <= 0
                        || explosionAction.radius() > 16
                        || explosionAction.damage() < 0) {
                    return false;
                }
            }
            else {
                return false;
            }
        }

        // Execute actions in the order defined in JSON.
        boolean anyApplied = false;

        for (AbilityAction action : ability.actions()) {
            if (action instanceof ApplyEffectAction effectAction) {
                var effectType = BuiltInRegistries.MOB_EFFECT
                        .get(effectAction.effect())
                        .orElseThrow();

                MobEffectInstance effect = new MobEffectInstance(
                        effectType,
                        effectAction.duration(),
                        effectAction.amplifier()
                );

                if (player.addEffect(effect)) {
                    anyApplied = true;
                }
            } else if (action instanceof HealAction healAction) {
                float healthBefore = player.getHealth();

                player.heal(healAction.amount());

                if (player.getHealth() > healthBefore) {
                    anyApplied = true;
                }
            } else if (action instanceof ExplosionAction explosionAction) {
                float radius = explosionAction.radius();
                float damage = explosionAction.damage();

                AABB area = player.getBoundingBox().inflate(radius);

                var targets = player.level().getEntitiesOfClass(
                        LivingEntity.class,
                        area,
                        entity -> entity != player && entity.isAlive()
                );

                for (LivingEntity target : targets) {
                    if (target.distanceToSqr(player) > radius * radius) {
                        continue;
                    }

                    if (damage > 0) {
                        target.hurt(
                                player.damageSources().playerAttack(player),
                                damage
                        );
                    }
                }
                if (player.level() instanceof ServerLevel serverLevel) {
                    if (explosionAction.damagesTerrain()) {
                        ExplosionDamageCalculator calculator = new ExplosionDamageCalculator() {
                            @Override
                            public boolean shouldDamageEntity(Explosion explosion, Entity entity) {
                                return false;
                            }

                            @Override
                            public float getKnockbackMultiplier(Entity entity) {
                                return 0.0F;
                            }
                        };

                        serverLevel.explode(
                                player,
                                null,
                                calculator,
                                player.getX(),
                                player.getY() + 0.5,
                                player.getZ(),
                                explosionAction.radius(),
                                false,
                                Level.ExplosionInteraction.BLOCK
                        );
                    }

                    serverLevel.sendParticles(
                            ParticleTypes.EXPLOSION_EMITTER,
                            player.getX(),
                            player.getY() + 0.5,
                            player.getZ(),
                            1,
                            0,
                            0,
                            0,
                            0
                    );

                    serverLevel.playSound(
                            null,
                            player.getX(),
                            player.getY(),
                            player.getZ(),
                            SoundEvents.GENERIC_EXPLODE,
                            SoundSource.PLAYERS,
                            1.0F,
                            1.0F
                    );

                }

                anyApplied = true;
                // Terrain destruction will be connected in the next step.
            }

        }

        if (!anyApplied) {
            return false;
        }

        // Creative players have unlimited Essence.
        if (!player.isCreative()) {
            player.setAttached(PlayerEssence.ESSENCE, currentEssence - cost);
        }

        return true;
    }
}