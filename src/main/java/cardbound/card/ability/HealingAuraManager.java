package cardbound.card.ability;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public final class HealingAuraManager {

    private record ActiveAura(
            ServerLevel level,
            UUID ownerId,
            float radius,
            float healing,
            int interval,
            long startTick,
            long expirationTick
    ) {}

    private static final List<ActiveAura> AURAS = new ArrayList<>();

    private HealingAuraManager() {
    }

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            Iterator<ActiveAura> iterator = AURAS.iterator();

            while (iterator.hasNext()) {
                ActiveAura aura = iterator.next();
                ServerLevel level = aura.level();
                long now = level.getGameTime();

                ServerPlayer owner = server.getPlayerList()
                        .getPlayer(aura.ownerId());

                if (now >= aura.expirationTick()
                        || owner == null
                        || !owner.isAlive()
                        || owner.level() != level) {
                    iterator.remove();
                    continue;
                }

                // Follow the player.
                Vec3 center = owner.position();

                // Magical aura particles every 5 ticks.
                if ((now - aura.startTick()) % 5 == 0) {
                    drawAura(level, center, aura.radius());
                }

                // Heal every configured interval.
                if ((now - aura.startTick()) % aura.interval() == 0) {
                    healTargets(level, owner, center, aura);
                }
            }
        });
    }

    public static void create(
            ServerLevel level,
            ServerPlayer player,
            float radius,
            int duration,
            float healing,
            int interval
    ) {
        long now = level.getGameTime();

        AURAS.add(new ActiveAura(
                level,
                player.getUUID(),
                radius,
                healing,
                interval,
                now,
                now + duration
        ));
    }

    private static void drawAura(
            ServerLevel level,
            Vec3 center,
            float radius
    ) {
        int points = 32;

        for (int i = 0; i < points; i++) {
            double angle = (Math.PI * 2.0 * i) / points;

            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;

            level.sendParticles(
                    ParticleTypes.END_ROD,
                    x,
                    center.y + 0.3,
                    z,
                    1,
                    0.1,
                    0.2,
                    0.1,
                    0.01
            );
        }
    }

    private static void healTargets(
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
                entity -> entity.isAlive()
                        && entity.position().distanceToSqr(center)
                        <= radius * radius
        );

        for (LivingEntity target : targets) {

            // Our custom summoned allies only.
            boolean isPlayer = target instanceof ServerPlayer;

            boolean isSummonedAlly =
                    target instanceof Mob mob
                            && mob instanceof SummonedAlly ally
                            && ally.cardbound$getOwnerUuid() != null;

// Peaceful creatures, including cows, sheep, pigs,
// villagers, naturally spawned wolves, etc.
            boolean isPassiveMob =
                    target instanceof net.minecraft.world.entity.AgeableMob
                            || target instanceof net.minecraft.world.entity.animal.wolf.Wolf
                            || target instanceof net.minecraft.world.entity.npc.villager.Villager;

            if (!isPlayer && !isSummonedAlly && !isPassiveMob) {
                continue;
            }

            if (target.getHealth() >= target.getMaxHealth()) {
                continue;
            }

            target.heal(aura.healing());

            level.sendParticles(
                    ParticleTypes.HEART,
                    target.getX(),
                    target.getY() + target.getBbHeight() + 0.2,
                    target.getZ(),
                    4,
                    0.3,
                    0.2,
                    0.3,
                    0.02
            );
        }
    }
}