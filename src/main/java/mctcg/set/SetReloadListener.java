
package mctcg.set;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class SetReloadListener implements PreparableReloadListener {

    @Override
    public CompletableFuture<Void> reload(
            SharedState currentReload,
            Executor taskExecutor,
            PreparationBarrier preparationBarrier,
            Executor reloadExecutor
    ) {
        return CompletableFuture.supplyAsync(() -> {

                    Map<Identifier, SetDefinition> definitions = new HashMap<>();

                    var resources = currentReload.resourceManager().listResources(
                            "sets",
                            id -> id.getPath().endsWith(".json")
                    );

                    for (var entry : resources.entrySet()) {
                        Identifier resourceId = entry.getKey();
                        var resource = entry.getValue();

                        String path = resourceId.getPath();

                        String setPath = path.substring(
                                "sets/".length(),
                                path.length() - ".json".length()
                        );

                        Identifier setId = Identifier.fromNamespaceAndPath(
                                resourceId.getNamespace(),
                                setPath
                        );

                        try (var reader = resource.openAsReader()) {
                            JsonElement json = JsonParser.parseReader(reader);

                            SetDefinition.CODEC.parse(JsonOps.INSTANCE, json)
                                    .resultOrPartial(error ->
                                            System.err.println(
                                                    "[MCTCG] Failed to load set " + setId + ": " + error
                                            )
                                    )
                                    .ifPresent(definition -> {
                                        definitions.put(setId, definition);
                                        System.out.println("[MCTCG] Loaded set: " + setId);
                                    });

                        } catch (Exception exception) {
                            System.err.println(
                                    "[MCTCG] Error reading set " + setId + ": " + exception.getMessage()
                            );
                        }
                    }

                    return definitions;

                }, taskExecutor).thenCompose(preparationBarrier::wait)
                .thenAcceptAsync(definitions -> {
                    SetRegistry.replaceAll(definitions);

                    System.out.println(
                            "[MCTCG] Registered " + definitions.size() + " set(s)"
                    );
                }, reloadExecutor);
    }
}
