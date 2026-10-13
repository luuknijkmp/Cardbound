package cardbound.card.ability;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public final class FireRingManager {

    private record ActiveRing(
            ServerLevel level,
            UUID ownerId,
            float radius,
            float damage,
            long expirationTick
    ) {}

    private static final List<ActiveRing> RINGS = new ArrayList<>();

    private FireRingManager() {
    }

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            Iterator<ActiveRing> iterator = RINGS.iterator();

            while (iterator.hasNext()) {
                ActiveRing ring = iterator.next();
                ServerLevel level = ring.level();
                long now = level.getGameTime();

                if (now >= ring.expirationTick()) {
                    iterator.remove();
                    continue;
                }

                ServerPlayer owner = server.getPlayerList()
                        .getPlayer(ring.ownerId());

                if (owner == null
                        || !owner.isAlive()
                        || owner.level() != level) {
                    iterator.remove();
                    continue;
                }

                // Follow the player's current position.
                Vec3 center = owner.position();

                // Draw the moving ring every 5 ticks.
                if (now % 5 == 0) {
                    drawRing(level, center, ring.radius());
                }

                // Damage and knock back enemies once per second.
                if (now % 20 == 0) {
                    damageEntities(level, owner, center, ring);
                }
            }
        });
    }

    public static void create(
            ServerLevel level,
            ServerPlayer player,
            float radius,
            int duration,
            float damage
    ) {
        RINGS.add(new ActiveRing(
                level,
                player.getUUID(),
                radius,
                damage,
                level.getGameTime() + duration
        ));
    }

    private static void drawRing(
            ServerLevel level,
            Vec3 center,
            float radius
    ) {
        int points = 48;

        for (int i = 0; i < points; i++) {
            double angle = (Math.PI * 2.0 * i) / points;

            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;

            level.sendParticles(
                    ParticleTypes.FLAME,
                    x,
                    center.y + 0.3,
                    z,
                    2,
                    0.05,
                    0.3,
                    0.05,
                    0.01
            );
        }
    }

    private static void damageEntities(
            ServerLevel level,
            ServerPlayer owner,
            Vec3 center,
            ActiveRing ring
    ) {
        double radius = ring.radius();

        AABB area = new AABB(
                center.x - radius - 1,
                center.y - 1,
                center.z - radius - 1,
                center.x + radius + 1,
                center.y + 3,
                center.z + radius + 1
        );

        List<LivingEntity> targets = level.getEntitiesOfClass(
                LivingEntity.class,
                area,
                entity -> entity != owner && entity.isAlive()
        );

        for (LivingEntity target : targets) {
            double dx = target.getX() - center.x;
            double dz = target.getZ() - center.z;

            double distance = Math.sqrt(dx * dx + dz * dz);

            // Only affect entities touching the ring.
            if (Math.abs(distance - radius) > 0.6) {
                continue;
            }

            // Fire damage.
            target.hurt(
                    owner.damageSources().onFire(),
                    ring.damage()
            );

            // Push the target AWAY from the player.
            if (distance > 0.001) {
                double knockbackStrength = 0.8;

                double pushX = (dx / distance) * knockbackStrength;
                double pushZ = (dz / distance) * knockbackStrength;

                target.push(pushX, 0.15, pushZ);
                target.hurtMarked = true;
            }
        }
    }
}