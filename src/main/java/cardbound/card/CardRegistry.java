
package cardbound.card;

import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CardRegistry {

    private static Map<Identifier, CardDefinition> cards = Map.of();

    public static Optional<CardDefinition> get(Identifier id) {
        return Optional.ofNullable(cards.get(id));
    }

    public static void replaceAll(Map<Identifier, CardDefinition> definitions) {
        cards = Map.copyOf(definitions);
    }

    public static boolean exists(Identifier id) {
        return cards.containsKey(id);
    }

    public static List<Identifier> getAllIds() {
        return List.copyOf(cards.keySet());
    }
}
