package mctcg.client;

import com.mojang.blaze3d.platform.InputConstants;
import mctcg.client.gui.ModSettingsScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class ModKeybindings {

    private static KeyMapping openSettings;

    private ModKeybindings() {
    }

    public static void initialize() {
        openSettings = KeyMappingHelper.registerKeyMapping(
                new KeyMapping(
                        "key.mctcg.open_settings",
                        InputConstants.Type.KEYSYM,
                        GLFW.GLFW_KEY_K,
                        KeyMapping.Category.MISC
                )
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openSettings.consumeClick()) {
                if (client.player != null) {
                    client.gui.setScreen(new ModSettingsScreen(null));
                }
            }
        });
    }
}