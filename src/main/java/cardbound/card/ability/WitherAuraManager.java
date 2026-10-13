package cardbound.card.ability;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public final class WitherAuraManager {

    private record ActiveAura(
            ServerLevel level,
            UUID ownerUuid,
            double radius,
            float damage,
            float healPerHit,
            long expirationTick
    ) {}

    private static final List<ActiveAura> ACTIVE = new ArrayList<>();

    private WitherAuraManager() {
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(
                WitherAuraManager::tick
        );
    }

    public static void start(
            ServerPlayer player,
            WitherAuraAction action
    ) {
        ACTIVE.removeIf(aura ->
                aura.ownerUuid().equals(player.getUUID())
        );

        ServerLevel level = player.level();

        ACTIVE.add(new ActiveAura(
                level,
                player.getUUID(),
                action.radius(),
                action.damage(),
                action.healPerHit(),
                level.getGameTime() + action.duration()
        ));
    }

    private static void tick(MinecraftServer server) {
        Iterator<ActiveAura> iterator = ACTIVE.iterator();

        while (iterator.hasNext()) {
            ActiveAura aura = iterator.next();
            ServerLevel level = aura.level();

            ServerPlayer owner = server.getPlayerList()
                    .getPlayer(aura.ownerUuid());

            if (owner == null
                    || !owner.isAlive()
                    || owner.level() != level
                    || level.getGameTime() >= aura.expirationTick()) {
                iterator.remove();
                continue;
            }

            long now = level.getGameTime();
            Vec3 center = owner.position();

            // Draw the moving soul ring every 5 ticks.
            if (now % 5 == 0) {
                drawRing(level, center, aura.radius());
            }

            // Damage enemies and steal health every second.
            if (now % 20 == 0) {
                damageAndHeal(level, owner, center, aura);
            }
        }
    }

    private static void drawRing(
            ServerLevel level,
            Vec3 center,
            double radius
    ) {
        int points = 48;

        for (int i = 0; i < points; i++) {
            double angle = (Math.PI * 2.0 * i) / points;

            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;

            level.sendParticles(
                    ParticleTypes.SOUL,
                    x,
                    center.y + 0.4,
                    z,
                    2,
                    0.05,
                    0.3,
                    0.05,
                    0.01
            );
        }
    }

    private static void damageAndHeal(
            ServerLevel level,
            ServerPlayer owner,
            Vec3 center,
            ActiveAura aura
    ) {
        double radius = aura.radius();

        AABB area = new AABB(
                center.x - radius,
                center.y - radius,
                center.z - radius,
                center.x + radius,
                center.y + radius,
                center.z + radius
        );

        List<LivingEntity> targets = level.getEntitiesOfClass(
                LivingEntity.class,
                area,
                entity ->
                        entity != owner
                                && entity.isAlive()
                                && !(entity instanceof SummonedAlly ally
                                && ally.cardbound$getOwnerUuid() != null)
                                && (entity instanceof Enemy
                                || entity instanceof ServerPlayer)
        );

        for (LivingEntity target : targets) {

            // Affect everything INSIDE the aura, not just its edge.
            if (target.distanceToSqr(owner) > radius * radius) {
                continue;
            }

            // Direct magic damage, no knockback code.
            float healthBefore = target.getHealth();

            target.hurt(
                    owner.damageSources().magic(),
                    aura.damage()
            );

            if (target.getHealth() < healthBefore) {
                owner.heal(aura.healPerHit());
            }
        }
    }
}