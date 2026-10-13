package cardbound.card.ability;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class PhantomDiveManager {

    private static final Map<UUID, Dive> ACTIVE_DIVES = new HashMap<>();
    private static final Map<UUID, Long> FALL_PROTECTION = new HashMap<>();

    private PhantomDiveManager() {
    }

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(PhantomDiveManager::tick);
    }

    public static void start(
            ServerPlayer player,
            float launchSpeed,
            float upwardBoost,
            float hitRadius,
            int duration,
            float damage,
            float knockback
    ) {
        ServerLevel level = player.level();
        Vec3 direction = player.getLookAngle().normalize();

        // Launch in the direction the player is looking.
        Vec3 velocity = direction.scale(launchSpeed);
        velocity = new Vec3(
                velocity.x,
                velocity.y + upwardBoost,
                velocity.z
        );

        player.setDeltaMovement(velocity);
        player.hurtMarked = true;

        long now = level.getGameTime();

        ACTIVE_DIVES.put(
                player.getUUID(),
                new Dive(
                        level,
                        player.position(),
                        now + duration,
                        hitRadius,
                        damage,
                        knockback
                )
        );

        // Fall protection during the dive and 3 seconds afterward.
        FALL_PROTECTION.put(player.getUUID(), now + duration + 60);
    }

    public static boolean isFallProtected(Player player) {
        Long expiresAt = FALL_PROTECTION.get(player.getUUID());

        return expiresAt != null
                && player.level().getGameTime() < expiresAt;
    }

    private static void tick(MinecraftServer server) {
        Iterator<Map.Entry<UUID, Dive>> iterator =
                ACTIVE_DIVES.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<UUID, Dive> entry = iterator.next();

            UUID playerId = entry.getKey();
            Dive dive = entry.getValue();

            ServerPlayer player = server.getPlayerList().getPlayer(playerId);

            if (player == null
                    || !player.isAlive()
                    || player.level() != dive.level
                    || dive.level.getGameTime() >= dive.expiresAt) {
                iterator.remove();
                continue;
            }

            Vec3 currentPosition = player.position();

            spawnParticles(dive.level, currentPosition);

            LivingEntity target = findTarget(
                    dive.level,
                    player,
                    dive.previousPosition,
                    currentPosition,
                    dive.hitRadius
            );

            if (target != null) {
                hitTarget(player, target, dive);
                iterator.remove();
                continue;
            }

            dive.previousPosition = currentPosition;
        }

        // Remove expired fall-protection entries.
        FALL_PROTECTION.entrySet().removeIf(entry -> {
            ServerPlayer player =
                    server.getPlayerList().getPlayer(entry.getKey());

            return player == null
                    || player.level().getGameTime() >= entry.getValue();
        });
    }

    private static LivingEntity findTarget(
            ServerLevel level,
            ServerPlayer player,
            Vec3 from,
            Vec3 to,
            float radius
    ) {
        // Cover the full distance traveled between ticks.
        AABB searchArea = new AABB(from, to).inflate(radius);

        LivingEntity closest = null;
        double closestDistance = Double.MAX_VALUE;

        for (LivingEntity target : level.getEntitiesOfClass(
                LivingEntity.class,
                searchArea,
                entity -> entity.isAlive()
                        && entity != player
                        && !(entity instanceof SummonedAlly ally
                        && ally.cardbound$getOwnerUuid() != null)
                        && (entity instanceof Enemy
                        || entity instanceof Player)
        )) {
            // Find the closest point along the player's movement.
            Vec3 movement = to.subtract(from);
            double lengthSquared = movement.lengthSqr();

            double t = lengthSquared > 0.000001
                    ? target.position().subtract(from).dot(movement)
                    / lengthSquared
                    : 0.0;

            t = Math.max(0.0, Math.min(1.0, t));

            Vec3 closestPoint = from.add(movement.scale(t));
            double distanceSquared =
                    target.position().distanceToSqr(closestPoint);

            double allowedDistance = radius + target.getBbWidth() * 0.5;

            if (distanceSquared <= allowedDistance * allowedDistance
                    && distanceSquared < closestDistance) {
                closest = target;
                closestDistance = distanceSquared;
            }
        }

        return closest;
    }

    private static void hitTarget(
            ServerPlayer player,
            LivingEntity target,
            Dive dive
    ) {
        target.hurt(
                player.damageSources().playerAttack(player),
                dive.damage
        );

        Vec3 direction = target.position()
                .subtract(player.position());

        Vec3 horizontal = new Vec3(
                direction.x,
                0.0,
                direction.z
        );

        if (horizontal.lengthSqr() < 0.0001) {
            Vec3 look = player.getLookAngle();
            horizontal = new Vec3(look.x, 0.0, look.z);
        }

        if (horizontal.lengthSqr() > 0.0001) {
            horizontal = horizontal.normalize();

            target.push(
                    horizontal.x * dive.knockback,
                    0.35,
                    horizontal.z * dive.knockback
            );

            target.hurtMarked = true;
        }

        // Slow the player after a successful collision.
        player.setDeltaMovement(
                player.getDeltaMovement().scale(0.25)
        );
        player.hurtMarked = true;

        dive.level.sendParticles(
                ParticleTypes.CLOUD,
                target.getX(),
                target.getY() + target.getBbHeight() * 0.5,
                target.getZ(),
                24,
                0.5,
                0.5,
                0.5,
                0.08
        );
    }

    private static void spawnParticles(
            ServerLevel level,
            Vec3 position
    ) {
        level.sendParticles(
                ParticleTypes.CLOUD,
                position.x,
                position.y + 0.5,
                position.z,
                4,
                0.25,
                0.25,
                0.25,
                0.01
        );
    }

    private static final class Dive {

        private final ServerLevel level;
        private Vec3 previousPosition;
        private final long expiresAt;
        private final float hitRadius;
        private final float damage;
        private final float knockback;

        private Dive(
                ServerLevel level,
                Vec3 previousPosition,
                long expiresAt,
                float hitRadius,
                float damage,
                float knockback
        ) {
            this.level = level;
            this.previousPosition = previousPosition;
            this.expiresAt = expiresAt;
            this.hitRadius = hitRadius;
            this.damage = damage;
            this.knockback = knockback;
        }
    }
}