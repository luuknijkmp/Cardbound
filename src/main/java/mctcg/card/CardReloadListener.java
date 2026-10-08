
package mctcg.card;

import net.minecraft.server.packs.resources.PreparableReloadListener;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import net.minecraft.server.packs.resources.ResourceManager;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

public class CardReloadListener implements PreparableReloadListener {

    @Override
    public CompletableFuture<Void> reload(
            SharedState currentReload,
            Executor taskExecutor,
            PreparationBarrier preparationBarrier,
            Executor reloadExecutor
    ) {
        return CompletableFuture


                .supplyAsync(() -> {
                    ResourceManager manager = currentReload.resourceManager();

                    var resources = manager.listResources(
                            "cards",
                            id -> id.getPath().endsWith(".json")
                    );

                    Map<Identifier, CardDefinition> definitions = new HashMap<>();

                    resources.forEach((resourceId, resource) -> {
                        String path = resourceId.getPath();

                        String cardPath = path.substring(
                                "cards/".length(),
                                path.length() - ".json".length()
                        );

                        Identifier cardId = Identifier.fromNamespaceAndPath(
                                resourceId.getNamespace(),
                                cardPath
                        );

                        try (var reader = resource.openAsReader()) {
                            JsonElement json = JsonParser.parseReader(reader);

                            CardDefinition.CODEC.parse(JsonOps.INSTANCE, json)
                                    .resultOrPartial(error ->
                                            System.err.println(
                                                    "[MCTCG] Invalid card " + cardId + ": " + error
                                            )
                                    )
                                    .ifPresent(definition -> {
                                        definitions.put(cardId, definition);
                                        System.out.println("[MCTCG] Loaded card: " + cardId);
                                    });

                        } catch (Exception e) {
                            System.err.println(
                                    "[MCTCG] Failed to read card " + cardId + ": " + e.getMessage()
                            );
                        }
                    });

                    return definitions;
                }, taskExecutor)

                .thenCompose(preparationBarrier::wait)

                .thenAcceptAsync(definitions -> {
                    CardRegistry.replaceAll(definitions);

                    System.out.println(
                            "[MCTCG] Registered " + definitions.size() + " card(s)"
                    );
                }, reloadExecutor);
    }
}
