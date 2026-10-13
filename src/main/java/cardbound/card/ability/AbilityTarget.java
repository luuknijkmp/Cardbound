package cardbound.card.ability;

import com.mojang.serialization.Codec;

public enum AbilityTarget {
    SELF("self"),
    LOOKED_AT("looked_at");

    public static final Codec<AbilityTarget> CODEC =
            Codec.STRING.xmap(
                    AbilityTarget::fromString,
                    AbilityTarget::getId
            );

    private final String id;

    AbilityTarget(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static AbilityTarget fromString(String id) {
        for (AbilityTarget target : values()) {
            if (target.id.equals(id)) {
                return target;
            }
        }

        throw new IllegalArgumentException(
                "Unknown ability target: " + id
        );
    }
}