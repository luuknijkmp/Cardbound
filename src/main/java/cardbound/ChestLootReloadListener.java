package cardbound;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class ChestLootReloadListener implements PreparableReloadListener {

    @Override
    public CompletableFuture<Void> reload(
            SharedState currentReload,
            Executor taskExecutor,
            PreparationBarrier preparationBarrier,
            Executor reloadExecutor
    ) {
        return CompletableFuture.supplyAsync(() -> {

                    Map<Identifier, ChestLootConfig.ChestRule> rules = new HashMap<>();

                    Identifier configId = Identifier.fromNamespaceAndPath(
                            "cardbound",
                            "loot_distribution/structure_chests.json"
                    );

                    var resource = currentReload.resourceManager().getResource(configId);

                    if (resource.isEmpty()) {
                        System.err.println("[CARDBOUND] Chest loot config not found: " + configId);
                        return rules;
                    }

                    try (var reader = resource.get().openAsReader()) {
                        JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                        JsonObject chests = root.getAsJsonObject("chests");

                        for (var entry : chests.entrySet()) {
                            Identifier chestId = Identifier.parse(entry.getKey());
                            JsonObject rule = entry.getValue().getAsJsonObject();

                            float vialChance = rule.get("vial_chance").getAsFloat();
                            float boosterChance = rule.get("booster_chance").getAsFloat();

                            var boosters = new ArrayList<Identifier>();

                            for (var booster : rule.getAsJsonArray("boosters")) {
                                boosters.add(Identifier.parse(booster.getAsString()));
                            }

                            rules.put(chestId, new ChestLootConfig.ChestRule(
                                    vialChance,
                                    boosterChance,
                                    List.copyOf(boosters)
                            ));
                        }

                        System.out.println(
                                "[CARDBOUND] Parsed chest loot rules for "
                                        + rules.size() + " loot tables"
                        );

                    } catch (Exception exception) {
                        System.err.println(
                                "[CARDBOUND] Failed to read chest loot config: "
                                        + exception.getMessage()
                        );
                    }

                    return rules;

                }, taskExecutor).thenCompose(preparationBarrier::wait)
                .thenAcceptAsync(ChestLootConfig::replaceAll, reloadExecutor);
    }
}