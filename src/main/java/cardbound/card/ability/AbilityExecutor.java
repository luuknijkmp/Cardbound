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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

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
                        || BuiltInRegistries.MOB_EFFECT.get(effectAction.effect()).isEmpty()
                        || !Float.isFinite(effectAction.range())
                        || effectAction.range() <= 0
                        || effectAction.range() > 64) {
                    return false;
                }

                if (TargetResolver.resolve(
                        player,
                        effectAction.target(),
                        effectAction.range()
                ).isEmpty()) {
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
            else if (action instanceof LightningAction lightningAction) {
                if (!Float.isFinite(lightningAction.range())
                        || lightningAction.range() <= 0
                        || lightningAction.range() > 64
                        || lightningAction.target() != AbilityTarget.LOOKED_AT) {
                    return false;
                }

                if (TargetResolver.resolve(
                        player,
                        lightningAction.target(),
                        lightningAction.range()
                ).isEmpty()) {
                    return false;
                }
            }
            else if (action instanceof SummonAction summonAction) {
                if (summonAction.count() <= 0
                        || summonAction.count() > 10
                        || summonAction.duration() <= 0
                        || summonAction.duration() > 12000
                        || BuiltInRegistries.ENTITY_TYPE.get(summonAction.entity()).isEmpty()) {
                    return false;
                }
            }
            else if (action instanceof DeathProtectionAction protectionAction) {
                if (protectionAction.duration() < 1
                        || protectionAction.duration() > 12000) {
                    return false;
                }
            }

            // New

            else {
                return false;
            }
        }

        // Execute actions in the order defined in JSON.
        boolean anyApplied = false;

        for (AbilityAction action : ability.actions()) {
            if (action instanceof ApplyEffectAction effectAction) {
                LivingEntity target = TargetResolver.resolve(
                        player,
                        effectAction.target(),
                        effectAction.range()
                ).orElseThrow();

                var effectType = BuiltInRegistries.MOB_EFFECT
                        .get(effectAction.effect())
                        .orElseThrow();

                MobEffectInstance effect = new MobEffectInstance(
                        effectType,
                        effectAction.duration(),
                        effectAction.amplifier()
                );

                if (target.addEffect(effect)) {
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
            } else if (action instanceof LightningAction lightningAction) {
                if (!(player.level() instanceof ServerLevel serverLevel)) {
                    return false;
                }

                LivingEntity target = TargetResolver.resolve(
                        player,
                        lightningAction.target(),
                        lightningAction.range()
                ).orElseThrow();

                LightningBolt lightning = EntityTypes.LIGHTNING_BOLT.create(
                        serverLevel,
                        EntitySpawnReason.TRIGGERED
                );

                if (lightning == null) {
                    return false;
                }

                lightning.setPos(
                        target.getX(),
                        target.getY(),
                        target.getZ()
                );

                serverLevel.addFreshEntity(lightning);

                anyApplied = true;
            }
            else if (action instanceof SummonAction summonAction) {
                if (!(player.level() instanceof ServerLevel serverLevel)) {
                    return false;
                }

                EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE
                        .get(summonAction.entity())
                        .orElseThrow()
                        .value();

                int spawned = 0;

                for (int i = 0; i < summonAction.count(); i++) {
                    var entity = entityType.create(
                            serverLevel,
                            EntitySpawnReason.TRIGGERED
                    );

                    if (!(entity instanceof Mob mob)
                            || !(mob instanceof SummonedAlly ally)) {
                        continue;
                    }

                    double angle = (Math.PI * 2.0 * i) / summonAction.count();
                    double x = player.getX() + Math.cos(angle) * 2.5;
                    double z = player.getZ() + Math.sin(angle) * 2.5;
                    double y = player.getY();

                    mob.setPos(x, y, z);

                    // Don't spawn inside blocks or other entities.
                    if (!serverLevel.noCollision(mob)) {
                        continue;
                    }

                    ally.cardbound$setOwnerUuid(player.getUUID());
                    ally.cardbound$setExpirationTick(
                            serverLevel.getGameTime() + summonAction.duration()
                    );
                    mob.setGlowingTag(true);

                    if (serverLevel.addFreshEntity(mob)) {
                        spawned++;
                    }
                }

                if (spawned > 0) {
                    anyApplied = true;
                }
            }
            else if (action instanceof DeathProtectionAction protectionAction) {
                player.addEffect(new MobEffectInstance(
                        CardEffects.PROTECTED,
                        protectionAction.duration(),
                        0
                ));

                anyApplied = true;
            }

            // New

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