
package cardbound.set;

import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.Optional;

public class SetRegistry {

    private static Map<Identifier, SetDefinition> sets = Map.of();

    public static Optional<SetDefinition> get(Identifier id) {
        return Optional.ofNullable(sets.get(id));
    }

    public static boolean exists(Identifier id) {
        return sets.containsKey(id);
    }

    public static void replaceAll(Map<Identifier, SetDefinition> newSets) {
        sets = Map.copyOf(newSets);
    }
}
