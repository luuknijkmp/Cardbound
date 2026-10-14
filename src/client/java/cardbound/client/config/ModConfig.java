package cardbound.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ModConfig {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private static final Path CONFIG_PATH =
            FabricLoader.getInstance()
                    .getConfigDir()
                    .resolve("cardbound.json");

    public enum EssenceHudStyle {
        FULL,
        MINIMAL
    }

    public enum EssenceHudVisibility {
        ALWAYS,
        CARD_IN_INVENTORY,
        CARD_IN_HAND
    }

    // HUD & Interface settings
    public EssenceHudStyle essenceHudStyle = EssenceHudStyle.FULL;
    public EssenceHudVisibility essenceHudVisibility = EssenceHudVisibility.ALWAYS;
    // Card opening preferences
    public boolean showCardReveal = true;

    // Card activation effects
    public boolean showCardActivationFlash = true;

    private static ModConfig instance = new ModConfig();

    private ModConfig() {
    }

    public static ModConfig get() {
        return instance;
    }

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            save();
            return;
        }

        try {
            String json = Files.readString(CONFIG_PATH);
            ModConfig loaded = GSON.fromJson(json, ModConfig.class);

            if (loaded != null) {
                instance = loaded;
            }

            // Handle missing or invalid settings gracefully.
            if (instance.essenceHudStyle == null) {
                instance.essenceHudStyle = EssenceHudStyle.FULL;
            }

            if (instance.essenceHudVisibility == null) {
                instance.essenceHudVisibility = EssenceHudVisibility.ALWAYS;
            }

        } catch (Exception e) {
            System.err.println("[Cardbound] Failed to load config: " + e.getMessage());
            instance = new ModConfig();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(instance));
        } catch (IOException e) {
            System.err.println("[Cardbound] Failed to save config: " + e.getMessage());
        }
    }
}