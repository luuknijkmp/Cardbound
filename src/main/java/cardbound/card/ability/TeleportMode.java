package cardbound.card.ability;

import com.mojang.serialization.Codec;

public enum TeleportMode {

    FORWARD("forward"),
    AWAY_FROM_ATTACKER("away_from_attacker"),
    BEHIND_TARGET("behind_target"),
    TO_TARGET("to_target");

    public static final Codec<TeleportMode> CODEC =
            Codec.STRING.xmap(
                    TeleportMode::fromString,
                    TeleportMode::getId
            );

    private final String id;

    TeleportMode(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static TeleportMode fromString(String id) {
        for (TeleportMode mode : values()) {
            if (mode.id.equals(id)) {
                return mode;
            }
        }

        throw new IllegalArgumentException(
                "Unknown teleport mode: " + id
        );
    }
}