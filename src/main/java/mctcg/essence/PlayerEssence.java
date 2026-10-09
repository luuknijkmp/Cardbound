
package mctcg.essence;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.Identifier;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public final class PlayerEssence {

    public static final int MAX_ESSENCE = 20;
    private static int tickCounter = 0;

    public static final AttachmentType<Integer> ESSENCE =
            AttachmentRegistry.create(
                    Identifier.fromNamespaceAndPath("mctcg", "essence"),
                    builder -> builder
                            .initializer(() -> MAX_ESSENCE)
                            .persistent(Codec.INT)
            );

    private PlayerEssence() {
    }

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCounter++;

            if (tickCounter < 80) {
                return;
            }

            tickCounter = 0;

            for (var player : server.getPlayerList().getPlayers()) {
                int currentEssence = player.getAttachedOrCreate(ESSENCE);

                if (currentEssence < MAX_ESSENCE) {
                    player.setAttached(ESSENCE, currentEssence + 1);
                }
            }
        });
    }
}
