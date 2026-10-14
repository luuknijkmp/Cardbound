package cardbound.card;

import cardbound.ModParticles;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public final class CardActivationRing {

    private static final int DURATION = 10;
    private static final int PARTICLES = 32;

    private static final List<ActiveRing> ACTIVE = new ArrayList<>();

    private CardActivationRing() {
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            Iterator<ActiveRing> iterator = ACTIVE.iterator();

            while (iterator.hasNext()) {
                ActiveRing ring = iterator.next();

                ServerPlayer player =
                        server.getPlayerList().getPlayer(ring.playerId());

                if (player == null || !player.isAlive()
                        || player.level() != ring.level()) {
                    iterator.remove();
                    continue;
                }

                int age = ring.age();

                if (age >= DURATION) {
                    iterator.remove();
                    continue;
                }

                double progress = age / (double) (DURATION - 1);
                double radius = 0.5 + progress * 2.0;

                double centerX = player.getX();
                double centerY = player.getY() + 0.15;
                double centerZ = player.getZ();

                for (int i = 0; i < PARTICLES; i++) {
                    double angle = 2.0 * Math.PI * i / PARTICLES;

                    double x = centerX + Math.cos(angle) * radius;
                    double z = centerZ + Math.sin(angle) * radius;

                    ring.level().sendParticles(
                            ModParticles.ARCANE_SPARK,
                            x, centerY, z,
                            1,
                            0.0, 0.0, 0.0,
                            0.0
                    );
                }

                ring.advance();
            }
        });
    }

    public static void start(ServerPlayer player) {
        ACTIVE.removeIf(ring ->
                ring.playerId().equals(player.getUUID())
        );

        ACTIVE.add(new ActiveRing(
                (ServerLevel) player.level(),
                player.getUUID()
        ));
    }

    private static final class ActiveRing {

        private final ServerLevel level;
        private final UUID playerId;
        private int age;

        private ActiveRing(ServerLevel level, UUID playerId) {
            this.level = level;
            this.playerId = playerId;
        }

        private ServerLevel level() {
            return level;
        }

        private UUID playerId() {
            return playerId;
        }

        private int age() {
            return age;
        }

        private void advance() {
            age++;
        }
    }
}