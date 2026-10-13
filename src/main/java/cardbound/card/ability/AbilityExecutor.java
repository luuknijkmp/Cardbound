package cardbound.card.ability;

import cardbound.card.CardAbility;
import cardbound.card.CardDefinition;
import cardbound.essence.PlayerEssence;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;

import java.util.Optional;

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

            } else if (action instanceof ExplosionAction explosionAction) {
                if (!Float.isFinite(explosionAction.radius())
                        || !Float.isFinite(explosionAction.damage())
                        || explosionAction.radius() <= 0
                        || explosionAction.radius() > 16
                        || explosionAction.damage() < 0) {
                    return false;
                }

            } else if (action instanceof LightningAction lightningAction) {
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

            } else if (action instanceof SummonAction summonAction) {
                if (summonAction.count() <= 0
                        || summonAction.count() > 10
                        || summonAction.duration() <= 0
                        || summonAction.duration() > 12000
                        || BuiltInRegistries.ENTITY_TYPE.get(summonAction.entity()).isEmpty()) {
                    return false;
                }

            } else if (action instanceof DeathProtectionAction protectionAction) {
                if (protectionAction.duration() < 1
                        || protectionAction.duration() > 12000) {
                    return false;
                }

            } else if (action instanceof DamageAction damageAction) {
                if (!Float.isFinite(damageAction.amount())
                        || damageAction.amount() <= 0
                        || damageAction.amount() > 100
                        || !Float.isFinite(damageAction.range())
                        || damageAction.range() <= 0
                        || damageAction.range() > 64) {
                    return false;
                }

                if (damageAction.target() != AbilityTarget.LOOKED_AT) {
                    return false;
                }

                if (TargetResolver.resolve(
                        player,
                        damageAction.target(),
                        damageAction.range()
                ).isEmpty()) {
                    return false;
                }
            } else if (action instanceof TeleportAction teleportAction) {
                if (!Float.isFinite(teleportAction.distance())
                        || teleportAction.distance() <= 0
                        || teleportAction.distance() > 64) {
                    return false;
                }

                if (!(player.level() instanceof ServerLevel serverLevel)) {
                    return false;
                }

                if (findTeleportDestination(
                        serverLevel,
                        player,
                        teleportAction
                ).isEmpty()) {
                    return false;
                }

            } else if (action instanceof WebTrapAction webTrapAction) {
                if (webTrapAction.duration() <= 0
                        || webTrapAction.duration() > 12000
                        || !Float.isFinite(webTrapAction.range())
                        || webTrapAction.range() <= 0
                        || webTrapAction.range() > 64) {
                    return false;
                }

                if (TargetResolver.resolve(
                        player,
                        AbilityTarget.LOOKED_AT,
                        webTrapAction.range()
                ).isEmpty()) {
                    return false;
                }

            } else if (action instanceof FireballVolleyAction volleyAction) {
                if (volleyAction.count() < 1
                        || volleyAction.count() > 10
                        || volleyAction.interval() < 1
                        || volleyAction.interval() > 40) {
                    return false;
                }

            } else if (action instanceof FireRingAction ringAction) {
                if (!Float.isFinite(ringAction.radius())
                        || ringAction.radius() <= 0
                        || ringAction.radius() > 16
                        || ringAction.duration() <= 0
                        || ringAction.duration() > 12000
                        || !Float.isFinite(ringAction.damage())
                        || ringAction.damage() <= 0
                        || ringAction.damage() > 100
                        || !(player instanceof net.minecraft.server.level.ServerPlayer)
                        || !(player.level() instanceof ServerLevel)) {
                    return false;
                }

            } else if (action instanceof WindBurstAction windAction) {
                if (!Float.isFinite(windAction.radius())
                        || windAction.radius() <= 0
                        || windAction.radius() > 16
                        || !Float.isFinite(windAction.strength())
                        || windAction.strength() <= 0
                        || windAction.strength() > 5) {
                    return false;
                }
            } else if (action instanceof WindLaunchAction launchAction) {
                if (!Float.isFinite(launchAction.strength())
                        || launchAction.strength() <= 0
                        || launchAction.strength() > 3.0F) {
                    return false;
                }
            } else if (action instanceof ItemMagnetAction magnetAction) {
                if (!Float.isFinite(magnetAction.radius())
                        || magnetAction.radius() <= 0
                        || magnetAction.radius() > 32) {
                    return false;
                }
            } else if (action instanceof HealingAuraAction auraAction) {
                if (!Float.isFinite(auraAction.radius())
                        || auraAction.radius() <= 0
                        || auraAction.radius() > 16
                        || auraAction.duration() <= 0
                        || auraAction.duration() > 12000
                        || !Float.isFinite(auraAction.healing())
                        || auraAction.healing() <= 0
                        || auraAction.healing() > 20
                        || auraAction.interval() <= 0
                        || auraAction.interval() > 200) {
                    return false;
                }
            } else if (action instanceof AreaPoisonAction poisonAction) {

                if (!Float.isFinite(poisonAction.radius())
                        || poisonAction.radius() <= 0
                        || poisonAction.radius() > 16
                        || poisonAction.duration() <= 0
                        || poisonAction.duration() > 12000
                        || poisonAction.amplifier() < 0
                        || poisonAction.amplifier() > 4
                        || poisonAction.damage() < 0
                        || poisonAction.damage() > 20) {
                    return false;
                }

            } else if (action instanceof DragonBreathAction breathAction) {

                if (!Float.isFinite(breathAction.range())
                        || breathAction.range() <= 0.0F
                        || breathAction.range() > 64.0F
                        || !Float.isFinite(breathAction.radius())
                        || breathAction.radius() <= 0.0F
                        || breathAction.radius() > 12.0F
                        || breathAction.duration() < 1
                        || breathAction.duration() > 1200
                        || !Float.isFinite(breathAction.damage())
                        || breathAction.damage() <= 0.0F
                        || breathAction.damage() > 20.0F
                        || breathAction.damageInterval() < 1
                        || breathAction.damageInterval() > 100) {
                    return false;
                }
            } else if (action instanceof AreaGlowingAction glowingAction) {

                if (!Float.isFinite(glowingAction.radius())
                        || glowingAction.radius() <= 0.0F
                        || glowingAction.radius() > 16.0F
                        || glowingAction.duration() <= 0
                        || glowingAction.duration() > 12000) {
                    return false;
                }
            } else if (action instanceof SandstormCurseAction sandstormAction) {

                if (!Float.isFinite(sandstormAction.radius())
                        || sandstormAction.radius() <= 0.0F
                        || sandstormAction.radius() > 16.0F
                        || sandstormAction.duration() <= 0
                        || sandstormAction.duration() > 12000) {
                    return false;
                }
            } else if (action instanceof LlamaSpitAction spitAction) {

                if (!Float.isFinite(spitAction.range())
                        || spitAction.range() <= 0.0F
                        || spitAction.range() > 64.0F) {
                    return false;
                }

                if (TargetResolver.resolve(
                        player,
                        AbilityTarget.LOOKED_AT,
                        spitAction.range()
                ).isEmpty()) {
                    return false;
                }
            } else if (action instanceof MagmaEruptionAction magmaAction) {

                if (!Float.isFinite(magmaAction.radius())
                        || magmaAction.radius() <= 0.0F
                        || magmaAction.radius() > 16.0F
                        || magmaAction.duration() < 1
                        || magmaAction.duration() > 1200
                        || !Float.isFinite(magmaAction.damage())
                        || magmaAction.damage() <= 0.0F
                        || magmaAction.damage() > 20.0F
                        || magmaAction.damageInterval() < 1
                        || magmaAction.damageInterval() > 100) {
                    return false;
                }
            } else if (action instanceof PhantomDiveAction diveAction) {

                if (!Float.isFinite(diveAction.launchSpeed())
                        || diveAction.launchSpeed() <= 0.0F
                        || diveAction.launchSpeed() > 5.0F
                        || !Float.isFinite(diveAction.upwardBoost())
                        || diveAction.upwardBoost() < 0.0F
                        || diveAction.upwardBoost() > 2.0F
                        || !Float.isFinite(diveAction.hitRadius())
                        || diveAction.hitRadius() <= 0.0F
                        || diveAction.hitRadius() > 5.0F
                        || diveAction.duration() < 1
                        || diveAction.duration() > 100
                        || !Float.isFinite(diveAction.damage())
                        || diveAction.damage() <= 0.0F
                        || diveAction.damage() > 20.0F
                        || !Float.isFinite(diveAction.knockback())
                        || diveAction.knockback() < 0.0F
                        || diveAction.knockback() > 5.0F) {
                    return false;
                }
            } else if (action instanceof SnowballVolleyAction snowballAction) {

                if (snowballAction.count() < 1
                        || snowballAction.count() > 20
                        || snowballAction.interval() < 1
                        || snowballAction.interval() > 40
                        || !Float.isFinite(snowballAction.speed())
                        || snowballAction.speed() <= 0.0F
                        || snowballAction.speed() > 5.0F
                        || !Float.isFinite(snowballAction.knockback())
                        || snowballAction.knockback() < 0.0F
                        || snowballAction.knockback() > 5.0F) {
                    return false;
                }

            } else {
                return false;
            }

        }

        // Execute actions in the order defined in JSON.
        // Remember the target before teleporting.
// Otherwise, teleportation might change who we're looking at.
        LivingEntity damageTarget = null;

        for (AbilityAction action : ability.actions()) {
            if (action instanceof DamageAction damageAction) {
                damageTarget = TargetResolver.resolve(
                        player,
                        damageAction.target(),
                        damageAction.range()
                ).orElse(null);

                if (damageTarget == null) {
                    return false;
                }

                break;
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

                // Visual feedback for effects applied to another entity.
                if (target.addEffect(effect)) {
                    anyApplied = true;

                    if (effectAction.target() == AbilityTarget.LOOKED_AT
                            && target.level() instanceof net.minecraft.server.level.ServerLevel effectLevel) {

                        effectLevel.sendParticles(
                                net.minecraft.core.particles.ParticleTypes.CLOUD,
                                target.getX(),
                                target.getY() + target.getBbHeight() * 0.5,
                                target.getZ(),
                                12,
                                target.getBbWidth() * 0.5,
                                target.getBbHeight() * 0.35,
                                target.getBbWidth() * 0.5,
                                0.02
                        );
                    }
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
                        ExplosionDamageCalculator calculator =
                                new ExplosionDamageCalculator() {
                                    @Override
                                    public boolean shouldDamageEntity(
                                            Explosion explosion,
                                            Entity entity
                                    ) {
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
                                radius,
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

            } else if (action instanceof SummonAction summonAction) {
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

                    if (!serverLevel.noCollision(mob)) {
                        continue;
                    }

                    ally.cardbound$setOwnerUuid(player.getUUID());
                    ally.cardbound$setExpirationTick(
                            serverLevel.getGameTime() + summonAction.duration()
                    );

// Summoned wolves behave like tamed wolves belonging to the player.
                    if (mob instanceof Wolf wolf) {
                        wolf.tame(player);
                        wolf.setOrderedToSit(false);
                    }

                    mob.setGlowingTag(true);

                    if (serverLevel.addFreshEntity(mob)) {
                        spawned++;
                    }
                }

                if (spawned > 0) {
                    anyApplied = true;
                }

            } else if (action instanceof DeathProtectionAction protectionAction) {
                player.addEffect(new MobEffectInstance(
                        CardEffects.PROTECTED,
                        protectionAction.duration(),
                        0
                ));

                anyApplied = true;

            } else if (action instanceof TeleportAction teleportAction) {
                if (!(player.level() instanceof ServerLevel serverLevel)) {
                    return false;
                }

                Optional<Vec3> destination = findTeleportDestination(
                        serverLevel,
                        player,
                        teleportAction
                );

                if (destination.isEmpty()) {
                    return false;
                }

                Vec3 origin = player.position();
                Vec3 pos = destination.get();

                player.teleportTo(pos.x, pos.y, pos.z);

                // Portal particles at departure.
                serverLevel.sendParticles(
                        ParticleTypes.PORTAL,
                        origin.x,
                        origin.y + 1.0,
                        origin.z,
                        35,
                        0.4,
                        0.8,
                        0.4,
                        0.15
                );

                // Portal particles at arrival.
                serverLevel.sendParticles(
                        ParticleTypes.PORTAL,
                        pos.x,
                        pos.y + 1.0,
                        pos.z,
                        35,
                        0.4,
                        0.8,
                        0.4,
                        0.15
                );

                // Enderman sounds at both locations.
                serverLevel.playSound(
                        null,
                        origin.x,
                        origin.y,
                        origin.z,
                        SoundEvents.ENDERMAN_TELEPORT,
                        SoundSource.PLAYERS,
                        1.0F,
                        1.0F
                );

                serverLevel.playSound(
                        null,
                        pos.x,
                        pos.y,
                        pos.z,
                        SoundEvents.ENDERMAN_TELEPORT,
                        SoundSource.PLAYERS,
                        1.0F,
                        1.0F
                );

                anyApplied = true;

            } else if (action instanceof DamageAction damageAction) {
                if (damageTarget == null || !damageTarget.isAlive()) {
                    return false;
                }

                if (!(player.level() instanceof ServerLevel serverLevel)) {
                    return false;
                }

                // Deal armor-bypassing magic damage.
                damageTarget.hurt(
                        player.damageSources().indirectMagic(player, player),
                        damageAction.amount()
                );

                // Purple void energy around the target.
                serverLevel.sendParticles(
                        ParticleTypes.PORTAL,
                        damageTarget.getX(),
                        damageTarget.getY() + damageTarget.getBbHeight() * 0.5,
                        damageTarget.getZ(),
                        60,
                        0.5,
                        0.7,
                        0.5,
                        0.2
                );
                anyApplied = true;

            } else if (action instanceof WebTrapAction webTrapAction) {
                if (!(player.level() instanceof ServerLevel serverLevel)) {
                    return false;
                }

                LivingEntity target = TargetResolver.resolve(
                        player,
                        AbilityTarget.LOOKED_AT,
                        webTrapAction.range()
                ).orElse(null);

                if (target == null || !target.isAlive()) {
                    return false;
                }

                BlockPos feet = target.blockPosition();
                BlockPos[] positions = {
                        feet,
                        feet.above(),
                        feet.north(),
                        feet.south(),
                        feet.east(),
                        feet.west()
                };

                int placed = 0;
                for (BlockPos pos : positions) {
                    if (TemporaryWebs.place(serverLevel, pos, webTrapAction.duration())) {
                        placed++;
                    }
                }

                if (placed > 0) {
                    serverLevel.sendParticles(
                            ParticleTypes.POOF,
                            target.getX(),
                            target.getY() + 0.8,
                            target.getZ(),
                            25,
                            0.5,
                            0.6,
                            0.5,
                            0.03
                    );
                    anyApplied = true;
                }
            } else if (action instanceof FireballVolleyAction volleyAction) {
                if (!(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)
                        || !(player.level() instanceof ServerLevel serverLevel)) {
                    return false;
                }

                FireballVolleyManager.launch(
                        serverLevel,
                        serverPlayer,
                        volleyAction.count(),
                        volleyAction.interval(),
                        volleyAction.explosive()
                );

                anyApplied = true;

            } else if (action instanceof FireRingAction ringAction) {
                if (!(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)
                        || !(player.level() instanceof ServerLevel serverLevel)) {
                    return false;
                }

                FireRingManager.create(
                        serverLevel,
                        serverPlayer,
                        ringAction.radius(),
                        ringAction.duration(),
                        ringAction.damage()
                );

                anyApplied = true;
            } else if (action instanceof WindBurstAction windAction) {
                if (!(player.level() instanceof ServerLevel serverLevel)) {
                    return false;
                }

                double radius = windAction.radius();
                double strength = windAction.strength();

                AABB area = player.getBoundingBox().inflate(radius);

                var targets = serverLevel.getEntitiesOfClass(
                        LivingEntity.class,
                        area,
                        entity -> entity != player && entity.isAlive()
                );

                for (LivingEntity target : targets) {
                    double dx = target.getX() - player.getX();
                    double dz = target.getZ() - player.getZ();

                    double distance = Math.sqrt(dx * dx + dz * dz);

                    if (distance < 0.001 || distance > radius) {
                        continue;
                    }

                    double pushX = (dx / distance) * strength;
                    double pushZ = (dz / distance) * strength;

                    target.push(pushX, 0.35, pushZ);
                    target.hurtMarked = true;
                }

                serverLevel.sendParticles(
                        ParticleTypes.GUST,
                        player.getX(),
                        player.getY() + 1.0,
                        player.getZ(),
                        35,
                        radius * 0.4,
                        0.5,
                        radius * 0.4,
                        0.1
                );

                anyApplied = true;
            } else if (action instanceof WindLaunchAction launchAction) {
                if (!(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)
                        || !(player.level() instanceof ServerLevel serverLevel)) {
                    return false;
                }

                Vec3 velocity = serverPlayer.getDeltaMovement();

                // Keep horizontal momentum, but launch upward.
                serverPlayer.setDeltaMovement(
                        velocity.x,
                        launchAction.strength(),
                        velocity.z
                );

                // Tell the client about the new velocity.
                serverPlayer.hurtMarked = true;

                // Wind burst at the player's feet.
                serverLevel.sendParticles(
                        ParticleTypes.GUST,
                        serverPlayer.getX(),
                        serverPlayer.getY() + 0.2,
                        serverPlayer.getZ(),
                        35,
                        0.6,
                        0.2,
                        0.6,
                        0.15
                );

                anyApplied = true;
            } else if (action instanceof ItemMagnetAction magnetAction) {
                if (!(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)
                        || !(player.level() instanceof ServerLevel serverLevel)) {
                    return false;
                }

                double radius = magnetAction.radius();

                AABB area = serverPlayer.getBoundingBox().inflate(radius);

                var items = serverLevel.getEntitiesOfClass(
                        net.minecraft.world.entity.item.ItemEntity.class,
                        area,
                        item -> item.isAlive()
                                && item.distanceToSqr(serverPlayer) <= radius * radius
                );

                int collected = 0;

                for (var item : items) {
                    var stack = item.getItem();

                    if (stack.isEmpty()) {
                        continue;
                    }

                    int before = stack.getCount();

                    // Adds as many items as possible to the inventory.
                    serverPlayer.getInventory().add(stack);

                    int pickedUp = before - stack.getCount();

                    if (pickedUp > 0) {
                        collected += pickedUp;

                        serverLevel.sendParticles(
                                ParticleTypes.HAPPY_VILLAGER,
                                item.getX(),
                                item.getY() + 0.2,
                                item.getZ(),
                                8,
                                0.2,
                                0.2,
                                0.2,
                                0.02
                        );

                        if (stack.isEmpty()) {
                            item.discard();
                        } else {
                            item.setItem(stack);
                        }
                    }
                }

                if (collected > 0) {
                    anyApplied = true;
                }
            } else if (action instanceof HealingAuraAction auraAction) {
                if (!(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)
                        || !(player.level() instanceof ServerLevel serverLevel)) {
                    return false;
                }

                HealingAuraManager.create(
                        serverLevel,
                        serverPlayer,
                        auraAction.radius(),
                        auraAction.duration(),
                        auraAction.healing(),
                        auraAction.interval()
                );

                anyApplied = true;
            } else if (action instanceof AreaPoisonAction poisonAction) {
                if (!(player.level() instanceof ServerLevel serverLevel)) {
                    return false;
                }

                double radius = poisonAction.radius();
                double radiusSquared = radius * radius;

                var nearbyTargets = serverLevel.getEntitiesOfClass(
                        LivingEntity.class,
                        player.getBoundingBox().inflate(radius),
                        target -> target != player
                                && target.isAlive()
                                && target.distanceToSqr(player) < radiusSquared
                                && !(target instanceof SummonedAlly ally
                                && ally.cardbound$getOwnerUuid() != null)
                                && (
                                target instanceof ServerPlayer
                                        || target instanceof net.minecraft.world.entity.monster.Enemy
                        )
                );

                for (LivingEntity target : nearbyTargets) {

                    // Initial bee sting — works even on poison-immune mobs.
                    if (poisonAction.damage() > 0) {
                        target.hurt(
                                serverLevel.damageSources().magic(),
                                poisonAction.damage()
                        );
                    }

                    // Poison follows normal Minecraft immunity rules.
                    target.addEffect(new MobEffectInstance(
                            MobEffects.POISON,
                            poisonAction.duration(),
                            poisonAction.amplifier()
                    ));

                    // One-time green burst around every affected target.
                    serverLevel.sendParticles(
                            net.minecraft.core.particles.ParticleTypes.WITCH,
                            target.getX(),
                            target.getY() + target.getBbHeight() * 0.5,
                            target.getZ(),
                            35,
                            target.getBbWidth() * 0.6,
                            target.getBbHeight() * 0.45,
                            target.getBbWidth() * 0.6,
                            0.02
                    );
                }

                anyApplied = true;


            } else if (action instanceof DragonBreathAction breathAction) {

                System.out.println("[Cardbound] Dragon Breath execution reached!");

                if (!(player.level() instanceof ServerLevel serverLevel)) {
                    System.out.println("[Cardbound] FAILED: Not a ServerLevel");
                    return false;
                }

                var targetPosition = TargetResolver.resolvePosition(
                        player,
                        breathAction.range()
                );

                System.out.println("[Cardbound] Target position: " + targetPosition);

                if (targetPosition.isEmpty()) {
                    System.out.println("[Cardbound] FAILED: No target position");
                    return false;
                }

                // Keep your existing DragonBreathManager.spawn(...)
                // and anyApplied = true; below this point.

                DragonBreathManager.spawn(
                        serverLevel,
                        player,
                        targetPosition.get(),
                        breathAction.radius(),
                        breathAction.duration(),
                        breathAction.damage(),
                        breathAction.damageInterval()
                );

                anyApplied = true;

                // ^ HERE
            } else if (action instanceof AreaGlowingAction glowingAction) {

                if (!(player.level() instanceof ServerLevel serverLevel)) {
                    return false;
                }

                double radius = glowingAction.radius();
                double radiusSquared = radius * radius;

                var nearbyTargets = serverLevel.getEntitiesOfClass(
                        LivingEntity.class,
                        player.getBoundingBox().inflate(radius),
                        target -> target != player
                                && target.isAlive()
                                && target.distanceToSqr(player) <= radiusSquared
                                && !(target instanceof SummonedAlly ally
                                && ally.cardbound$getOwnerUuid() != null)
                                && (target instanceof ServerPlayer
                                || target instanceof net.minecraft.world.entity.monster.Enemy)
                );

                for (LivingEntity target : nearbyTargets) {

                    target.addEffect(new MobEffectInstance(
                            MobEffects.GLOWING,
                            glowingAction.duration(),
                            0
                    ));

                    serverLevel.sendParticles(
                            ParticleTypes.END_ROD,
                            target.getX(),
                            target.getY() + target.getBbHeight() * 0.5,
                            target.getZ(),
                            18,
                            target.getBbWidth() * 0.5,
                            target.getBbHeight() * 0.4,
                            target.getBbWidth() * 0.5,
                            0.02
                    );
                }

                anyApplied = true;
            }
            else if (action instanceof SandstormCurseAction sandstormAction) {

                if (!(player.level() instanceof ServerLevel serverLevel)) {
                    return false;
                }

                double radius = sandstormAction.radius();
                double radiusSquared = radius * radius;

                var nearbyTargets = serverLevel.getEntitiesOfClass(
                        LivingEntity.class,
                        player.getBoundingBox().inflate(radius),
                        target -> target != player
                                && target.isAlive()
                                && target.distanceToSqr(player) <= radiusSquared
                                && !(target instanceof SummonedAlly ally
                                && ally.cardbound$getOwnerUuid() != null)
                                && (target instanceof ServerPlayer
                                || target instanceof net.minecraft.world.entity.monster.Enemy)
                );

                for (LivingEntity target : nearbyTargets) {

                    // Blindness I
                    target.addEffect(new MobEffectInstance(
                            MobEffects.BLINDNESS,
                            sandstormAction.duration(),
                            0
                    ));

                    // Slowness II
                    target.addEffect(new MobEffectInstance(
                            MobEffects.SLOWNESS,
                            sandstormAction.duration(),
                            1
                    ));

                    // Sandstorm particles around each affected enemy.
                    serverLevel.sendParticles(
                            new net.minecraft.core.particles.BlockParticleOption(
                                    ParticleTypes.FALLING_DUST,
                                    net.minecraft.world.level.block.Blocks.SAND.defaultBlockState()
                            ),
                            target.getX(),
                            target.getY() + target.getBbHeight() * 0.5,
                            target.getZ(),
                            25,
                            target.getBbWidth() * 0.6,
                            target.getBbHeight() * 0.4,
                            target.getBbWidth() * 0.6,
                            0.04
                    );


                }


                anyApplied = true;
            } else if (action instanceof LlamaSpitAction spitAction) {

                if (!(player.level() instanceof ServerLevel serverLevel)) {
                    return false;
                }

                LivingEntity target = TargetResolver.resolve(
                        player,
                        AbilityTarget.LOOKED_AT,
                        spitAction.range()
                ).orElse(null);

                if (target == null || !target.isAlive()) {
                    return false;
                }

                // Spawn the projectile in front of the player's face.
                Vec3 start = player.getEyePosition()
                        .add(player.getLookAngle().scale(0.8));

                // Aim toward the target's center.
                Vec3 destination = target.position()
                        .add(0.0, target.getBbHeight() * 0.5, 0.0);

                Vec3 direction = destination.subtract(start).normalize();

                CardboundLlamaSpit spit = new CardboundLlamaSpit(
                        serverLevel,
                        player
                );

                spit.setPos(start.x, start.y, start.z);

                spit.shoot(
                        direction.x,
                        direction.y,
                        direction.z,
                        1.5F,
                        0.0F
                );

                serverLevel.addFreshEntity(spit);

                anyApplied = true;
            }
            else if (action instanceof MagmaEruptionAction magmaAction) {

                if (!(player.level() instanceof ServerLevel serverLevel)) {
                    return false;
                }

                MagmaEruptionManager.create(
                        serverLevel,
                        player,
                        magmaAction.radius(),
                        magmaAction.duration(),
                        magmaAction.damage(),
                        magmaAction.damageInterval()
                );

                anyApplied = true;
            }
            else if (action instanceof PhantomDiveAction diveAction) {

                if (!(player instanceof ServerPlayer serverPlayer)) {
                    return false;
                }

                PhantomDiveManager.start(
                        serverPlayer,
                        diveAction.launchSpeed(),
                        diveAction.upwardBoost(),
                        diveAction.hitRadius(),
                        diveAction.duration(),
                        diveAction.damage(),
                        diveAction.knockback()
                );

                anyApplied = true;

            } else if (action instanceof SnowballVolleyAction snowballAction) {

                if (player instanceof ServerPlayer serverPlayer) {
                    SnowballVolleyManager.start(serverPlayer, snowballAction);
                    anyApplied = true;
                }
            }
        }



        if (!anyApplied) {
            return false;
        }

        // Creative players have unlimited Essence.
        if (!player.isCreative()) {
            player.setAttached(
                    PlayerEssence.ESSENCE,
                    currentEssence - cost
            );
        }

        return true;
    }


    private static Optional<Vec3> findTeleportDestination(
            ServerLevel serverLevel,
            Player player,
            TeleportAction action
    ) {
        return switch (action.mode()) {
            case FORWARD -> TeleportResolver.findForwardDestination(
                    serverLevel,
                    player,
                    action.distance(),
                    action.throughWalls()
            );

            case AWAY_FROM_ATTACKER -> TeleportResolver.findEscapeDestination(
                    serverLevel,
                    player,
                    action.distance(),
                    action.throughWalls()
            );

            // We'll implement these when we reach cards 013 and 014.
            case BEHIND_TARGET -> TeleportResolver.findBehindTargetDestination(
                    serverLevel,
                    player,
                    action.distance(),
                    action.throughWalls()
            );

            case TO_TARGET -> TeleportResolver.findToTargetDestination(
                    serverLevel,
                    player,
                    action.distance(),
                    action.throughWalls()
            );
        };
    }
}
