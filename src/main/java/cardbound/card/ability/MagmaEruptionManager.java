package cardbound.card.ability;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public final class MagmaEruptionManager {

    private static final List<MagmaZone> ZONES = new ArrayList<>();

    private MagmaEruptionManager() {
    }

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(
                MagmaEruptionManager::tick
        );
    }

    public static void create(
            ServerLevel level,
            Player owner,
            float radius,
            int duration,
            float damage,
            int damageInterval
    ) {
        long now = level.getGameTime();

        ZONES.add(new MagmaZone(
                level,
                owner.getUUID(),
                owner.position(),
                radius,
                now + duration,
                damage,
                damageInterval,
                now + damageInterval
        ));
    }

    private static void tick(MinecraftServer server) {
        Iterator<MagmaZone> iterator = ZONES.iterator();

        while (iterator.hasNext()) {
            MagmaZone zone = iterator.next();

            if (server.getLevel(zone.level.dimension()) != zone.level) {
                iterator.remove();
                continue;
            }

            long now = zone.level.getGameTime();

            if (now >= zone.expiresAt) {
                iterator.remove();
                continue;
            }

            spawnParticles(zone);

            if (now >= zone.nextDamageTick) {
                damageTargets(zone);
                zone.nextDamageTick = now + zone.damageInterval;
            }
        }
    }

    private static void spawnParticles(MagmaZone zone) {
        ServerLevel level = zone.level;
        Vec3 center = zone.center;

        // Flames across the magma zone.
        level.sendParticles(
                ParticleTypes.FLAME,
                center.x,
                center.y + 0.15,
                center.z,
                18,
                zone.radius * 0.65,
                0.1,
                zone.radius * 0.65,
                0.02
        );

        // Occasional lava splashes.
        if (level.getRandom().nextInt(4) == 0) {
            level.sendParticles(
                    ParticleTypes.LAVA,
                    center.x,
                    center.y + 0.2,
                    center.z,
                    6,
                    zone.radius * 0.5,
                    0.1,
                    zone.radius * 0.5,
                    0.02
            );
        }
    }

    private static void damageTargets(MagmaZone zone) {
        ServerLevel level = zone.level;
        Vec3 center = zone.center;

        double radius = zone.radius;
        double radiusSquared = radius * radius;

        AABB area = new AABB(
                center.x - radius,
                center.y - 1.0,
                center.z - radius,
                center.x + radius,
                center.y + 3.0,
                center.z + radius
        );

        Player owner = level.getPlayerByUUID(zone.ownerId);

        var targets = level.getEntitiesOfClass(
                LivingEntity.class,
                area,
                target -> target.isAlive()
                        && !target.getUUID().equals(zone.ownerId)
                        && !(target instanceof SummonedAlly ally
                        && ally.cardbound$getOwnerUuid() != null)
                        && (target instanceof Enemy
                        || target instanceof Player)
        );

        for (LivingEntity target : targets) {
            double dx = target.getX() - center.x;
            double dz = target.getZ() - center.z;

            if (dx * dx + dz * dz > radiusSquared) {
                continue;
            }

            if (owner != null) {
                target.hurt(
                        owner.damageSources().indirectMagic(owner, owner),
                        zone.damage
                );
            } else {
                target.hurt(
                        level.damageSources().magic(),
                        zone.damage
                );
            }
        }
    }

    private static final class MagmaZone {

        private final ServerLevel level;
        private final UUID ownerId;
        private final Vec3 center;
        private final float radius;
        private final long expiresAt;
        private final float damage;
        private final int damageInterval;

        private long nextDamageTick;

        private MagmaZone(
                ServerLevel level,
                UUID ownerId,
                Vec3 center,
                float radius,
                long expiresAt,
                float damage,
                int damageInterval,
                long nextDamageTick
        ) {
            this.level = level;
            this.ownerId = ownerId;
            this.center = center;
            this.radius = radius;
            this.expiresAt = expiresAt;
            this.damage = damage;
            this.damageInterval = damageInterval;
            this.nextDamageTick = nextDamageTick;
        }
    }
}