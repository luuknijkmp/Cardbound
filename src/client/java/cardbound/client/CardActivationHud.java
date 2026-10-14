package cardbound.client;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import cardbound.client.config.ModConfig;

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

                    if (!ModConfig.get().showCardActivationFlash) {
                        return;
                    }

                    Minecraft minecraft = Minecraft.getInstance();

                    if (minecraft.player == null
                            || !CardActivationAnimation.isActive()) {
                        return;
                    }

                    float progress = CardActivationAnimation.progress();

// Our activation animation lasts 400 ms.
// 250 / 400 = 0.625
                    float flashStart = 0.625F;

// Flash begins exactly 250 ms after activation.
                    float flashProgress = Math.max(
                            0.0F,
                            (progress - flashStart) / (1.0F - flashStart)
                    );

                    float fade = 1.0F - flashProgress;
                    fade *= fade;

                    int alpha = progress < flashStart
                            ? 0
                            : Math.round(fade * 38.0F);

                    if (alpha > 0) {
                        int color = (alpha << 24) | 0xD8DEFF;

                        graphics.fill(
                                0,
                                0,
                                graphics.guiWidth(),
                                graphics.guiHeight(),
                                color
                        );
                    }

                    //int x = graphics.guiWidth() / 2 - 115;
                    //int y = graphics.guiHeight() - 35;

                    //graphics.pose().pushMatrix();

                    //graphics.pose().translate(x, y);
                    //graphics.pose().scale(2.0F, 2.0F);

                    //graphics.item(
                    //        CardActivationAnimation.getCard(),
                    //        0,
                    //        0
                    //);

                    //graphics.pose().popMatrix();
                }
        );
    }
}