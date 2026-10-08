
package mctcg.booster;

import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.Optional;

public class BoosterRegistry {

    private static Map<Identifier, BoosterDefinition> boosters = Map.of();

    public static Optional<BoosterDefinition> get(Identifier id) {
        return Optional.ofNullable(boosters.get(id));
    }

    public static boolean exists(Identifier id) {
        return boosters.containsKey(id);
    }

    public static void replaceAll(Map<Identifier, BoosterDefinition> newBoosters) {
        boosters = Map.copyOf(newBoosters);
    }
}
