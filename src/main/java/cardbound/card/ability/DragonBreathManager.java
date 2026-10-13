package cardbound.card.ability;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.PowerParticleOption;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public final class DragonBreathManager {

    private static final class BreathCloud {

        final ServerLevel level;
        final UUID ownerId;
        final Vec3 center;
        final float radius;
        final long expiresAt;
        final float damage;
        final int damageInterval;

        long nextDamageTick;

        BreathCloud(
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

    private static final List<BreathCloud> CLOUDS = new ArrayList<>();

    private DragonBreathManager() {
    }

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {

            Iterator<BreathCloud> iterator = CLOUDS.iterator();

            while (iterator.hasNext()) {
                BreathCloud cloud = iterator.next();
                long now = cloud.level.getGameTime();

                if (now >= cloud.expiresAt) {
                    iterator.remove();
                    continue;
                }

                spawnParticles(cloud);

                if (now >= cloud.nextDamageTick) {
                    damageTargets(cloud);
                    cloud.nextDamageTick = now + cloud.damageInterval;
                }
            }
        });
    }

    public static void spawn(
            ServerLevel level,
            Player owner,
            Vec3 center,
            float radius,
            int duration,
            float damage,
            int damageInterval
    ) {
        long now = level.getGameTime();

        CLOUDS.add(new BreathCloud(
                level,
                owner.getUUID(),
                center,
                radius,
                now + duration,
                damage,
                damageInterval,
                now + damageInterval
        ));
    }

    private static void spawnParticles(BreathCloud cloud) {
        Vec3 center = cloud.center;
        double radius = cloud.radius;

        cloud.level.sendParticles(
                PowerParticleOption.create(
                        ParticleTypes.DRAGON_BREATH,
                        1.0F
                ),
                center.x,
                center.y + 0.5,
                center.z,
                35,
                radius * 0.55,
                0.45,
                radius * 0.55,
                0.01
        );
    }

    private static void damageTargets(BreathCloud cloud) {
        ServerLevel level = cloud.level;
        Vec3 center = cloud.center;
        double radius = cloud.radius;

        AABB area = new AABB(
                center.x - radius,
                center.y - 1.0,
                center.z - radius,
                center.x + radius,
                center.y + 3.0,
                center.z + radius
        );

        Player owner = level.getServer()
                .getPlayerList()
                .getPlayer(cloud.ownerId);

        for (LivingEntity target : level.getEntitiesOfClass(
                LivingEntity.class,
                area
        )) {
            // Never damage the caster.
            if (target.getUUID().equals(cloud.ownerId)) {
                continue;
            }

            // Only hostile mobs and other players.
            if (!(target instanceof Monster)
                    && !(target instanceof Player)) {
                continue;
            }

            // Protect all Cardbound summoned allies.
            if (target instanceof SummonedAlly ally
                    && ally.cardbound$getOwnerUuid() != null) {
                continue;
            }

            // Keep the damage area circular.
            double dx = target.getX() - center.x;
            double dz = target.getZ() - center.z;

            if (dx * dx + dz * dz > radius * radius) {
                continue;
            }

            if (owner != null) {
                target.hurt(
                        owner.damageSources().indirectMagic(owner, owner),
                        cloud.damage
                );
            } else {
                target.hurt(
                        level.damageSources().magic(),
                        cloud.damage
                );
            }
        }
    }
}