package cardbound.card.ability;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public final class SnowballVolleyManager {

    private static final List<ActiveVolley> ACTIVE_VOLLEYS =
            new ArrayList<>();

    private SnowballVolleyManager() {
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(
                SnowballVolleyManager::tick
        );
    }

    public static void start(
            ServerPlayer player,
            SnowballVolleyAction action
    ) {
        ACTIVE_VOLLEYS.add(new ActiveVolley(
                player.getUUID(),
                player.level().getGameTime(),
                action.count(),
                action.interval(),
                action.speed(),
                action.knockback()
        ));
    }

    private static void tick(MinecraftServer server) {
        Iterator<ActiveVolley> iterator = ACTIVE_VOLLEYS.iterator();

        while (iterator.hasNext()) {
            ActiveVolley volley = iterator.next();

            ServerPlayer player =
                    server.getPlayerList().getPlayer(volley.playerId);

            if (player == null || !player.isAlive()) {
                iterator.remove();
                continue;
            }

            ServerLevel level = player.level();
            long currentTick = level.getGameTime();

            if (currentTick < volley.nextShotTick) {
                continue;
            }

            fireSnowball(
                    level,
                    player,
                    volley.speed,
                    volley.knockback
            );

            volley.remainingShots--;

            if (volley.remainingShots <= 0) {
                iterator.remove();
            } else {
                volley.nextShotTick =
                        currentTick + volley.interval;
            }
        }
    }

    private static void fireSnowball(
            ServerLevel level,
            ServerPlayer player,
            float speed,
            float knockback
    ) {
        Vec3 direction = player.getLookAngle().normalize();

        CardboundSnowball snowball =
                new CardboundSnowball(level, player, knockback);

        Vec3 spawnPosition = player.getEyePosition()
                .add(direction.scale(0.5));

        snowball.setPos(
                spawnPosition.x,
                spawnPosition.y,
                spawnPosition.z
        );

        snowball.setDeltaMovement(direction.scale(speed));

        level.addFreshEntity(snowball);
    }

    private static final class ActiveVolley {

        private final UUID playerId;
        private final int interval;
        private final float speed;
        private final float knockback;

        private int remainingShots;
        private long nextShotTick;

        private ActiveVolley(
                UUID playerId,
                long startTick,
                int count,
                int interval,
                float speed,
                float knockback
        ) {
            this.playerId = playerId;
            this.nextShotTick = startTick;
            this.remainingShots = count;
            this.interval = interval;
            this.speed = speed;
            this.knockback = knockback;
        }
    }
}