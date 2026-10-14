package cardbound.client;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public final class CardActivationHud {

    private CardActivationHud() {
    }

    public static void initialize() {
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.FOOD_BAR,
                Identifier.fromNamespaceAndPath(
                        "cardbound", "card_activation_hud"
                ),
                (graphics, deltaTracker) -> {

                    Minecraft minecraft = Minecraft.getInstance();

                    if (minecraft.player == null
                            || !CardActivationAnimation.isActive()) {
                        return;
                    }

                    int x = graphics.guiWidth() / 2 - 115;
                    int y = graphics.guiHeight() - 35;

                    graphics.pose().pushMatrix();

                    graphics.pose().translate(x, y);
                    graphics.pose().scale(2.0F, 2.0F);

                    graphics.item(
                            CardActivationAnimation.getCard(),
                            0,
                            0
                    );

                    graphics.pose().popMatrix();
                }
        );
    }
}