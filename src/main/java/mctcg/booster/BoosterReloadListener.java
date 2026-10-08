
package mctcg.booster;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class BoosterReloadListener implements PreparableReloadListener {

    @Override
    public CompletableFuture<Void> reload(
            SharedState currentReload,
            Executor taskExecutor,
            PreparationBarrier preparationBarrier,
            Executor reloadExecutor
    ) {
        return CompletableFuture.supplyAsync(() -> {

                    Map<Identifier, BoosterDefinition> definitions = new HashMap<>();

                    var resources = currentReload.resourceManager().listResources(
                            "boosters",
                            id -> id.getPath().endsWith(".json")
                    );

                    for (var entry : resources.entrySet()) {
                        Identifier resourceId = entry.getKey();
                        var resource = entry.getValue();

                        String path = resourceId.getPath();

                        String boosterPath = path.substring(
                                "boosters/".length(),
                                path.length() - ".json".length()
                        );

                        Identifier boosterId = Identifier.fromNamespaceAndPath(
                                resourceId.getNamespace(),
                                boosterPath
                        );

                        try (var reader = resource.openAsReader()) {
                            JsonElement json = JsonParser.parseReader(reader);

                            BoosterDefinition.CODEC.parse(JsonOps.INSTANCE, json)
                                    .resultOrPartial(error ->
                                            System.err.println(
                                                    "[MCTCG] Failed to load booster "
                                                            + boosterId + ": " + error
                                            )
                                    )
                                    .ifPresent(definition -> {
                                        definitions.put(boosterId, definition);
                                        System.out.println(
                                                "[MCTCG] Loaded booster: " + boosterId
                                        );
                                    });

                        } catch (Exception exception) {
                            System.err.println(
                                    "[MCTCG] Error reading booster "
                                            + boosterId + ": " + exception.getMessage()
                            );
                        }
                    }

                    return definitions;

                }, taskExecutor).thenCompose(preparationBarrier::wait)
                .thenAcceptAsync(definitions -> {
                    BoosterRegistry.replaceAll(definitions);

                    System.out.println(
                            "[MCTCG] Registered " + definitions.size() + " booster(s)"
                    );
                }, reloadExecutor);
    }
}
