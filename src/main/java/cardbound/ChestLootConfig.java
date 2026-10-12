package cardbound;

import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Map;

public final class ChestLootConfig {

    public record ChestRule(
            float vialChance,
            float boosterChance,
            List<Identifier> boosters
    ) {}

    private static volatile Map<Identifier, ChestRule> rules = Map.of();

    public static ChestRule get(Identifier chestId) {
        return rules.get(chestId);
    }

    public static void replaceAll(Map<Identifier, ChestRule> newRules) {
        rules = Map.copyOf(newRules);
    }

    private ChestLootConfig() {}
}