package cardbound.card.ability;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public final class FireballVolleyManager {

    private record PendingShot(
            ServerLevel level,
            UUID playerId,
            Vec3 direction,
            long fireAtTick,
            boolean explosive
    ) {}

    private static final List<PendingShot> PENDING = new ArrayList<>();

    private FireballVolleyManager() {
    }

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            Iterator<PendingShot> iterator = PENDING.iterator();

            while (iterator.hasNext()) {
                PendingShot shot = iterator.next();

                if (shot.level().getGameTime() < shot.fireAtTick()) {
                    continue;
                }

                ServerPlayer player = server.getPlayerList()
                        .getPlayer(shot.playerId());

                if (player != null
                        && player.isAlive()
                        && player.level() == shot.level()) {
                    fire(shot.level(), player, shot.direction(), shot.explosive());
                }

                iterator.remove();
            }
        });
    }

    public static void launch(
            ServerLevel level,
            ServerPlayer player,
            int count,
            int interval,
            boolean explosive
    ) {
        Vec3 direction = player.getLookAngle().normalize();
        long now = level.getGameTime();

        // First shot launches immediately.
        fire(level, player, direction, explosive);

        // Schedule the remaining shots.
        for (int i = 1; i < count; i++) {
            PENDING.add(new PendingShot(
                    level,
                    player.getUUID(),
                    direction,
                    now + (long) i * interval,
                    explosive
            ));
        }
    }

    private static void fire(
            ServerLevel level,
            ServerPlayer player,
            Vec3 direction,
            boolean explosive
    ) {
        Vec3 spawnPos = player.getEyePosition()
                .add(direction.scale(0.8));

        if (explosive) {
            // Ghast-style fireball with a small, terrain-damaging explosion.
            LargeFireball fireball = new LargeFireball(
                    level,
                    player,
                    direction,
                    1
            );

            fireball.setPos(
                    spawnPos.x,
                    spawnPos.y,
                    spawnPos.z
            );

            level.addFreshEntity(fireball);

        } else {
            // Original Blazing Inferno behavior: small, non-explosive fireballs.
            SmallFireball fireball = new SmallFireball(
                    level,
                    player,
                    direction
            );

            fireball.setPos(
                    spawnPos.x,
                    spawnPos.y,
                    spawnPos.z
            );

            level.addFreshEntity(fireball);
        }
    }
}