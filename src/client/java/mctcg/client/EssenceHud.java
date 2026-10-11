package mctcg.client;

import mctcg.ModItems;
import mctcg.essence.PlayerEssence;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import mctcg.client.config.ModConfig;

public final class EssenceHud {

    public enum DisplayStyle {
        FULL_BAR,
        MINIMAL
    }

    private static final int DROP_SIZE = 9;
    private static final int DROP_SPACING = 8;
    private static final int DROP_COUNT = 10;

    private static final Identifier DROP_EMPTY =
            Identifier.fromNamespaceAndPath(
                    "mctcg", "textures/gui/essence_drop_empty.png"
            );

    private static final Identifier DROP_HALF =
            Identifier.fromNamespaceAndPath(
                    "mctcg", "textures/gui/essence_drop_half.png"
            );

    private static final Identifier DROP_FULL =
            Identifier.fromNamespaceAndPath(
                    "mctcg", "textures/gui/essence_drop_full.png"
            );

    private EssenceHud() {
    }

    public static void initialize() {
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath("mctcg", "essence_hud"),
                (graphics, deltaTracker) -> {

                    Minecraft minecraft = Minecraft.getInstance();

                    if (minecraft.player == null) {
                        return;
                    }

                    // Creative Mode has unlimited Essence, so hide the HUD.
                    if (minecraft.player.isCreative()) {
                        return;
                    }

                    int essence = minecraft.player.getAttachedOrCreate(
                            PlayerEssence.ESSENCE
                    );

                    ModConfig config = ModConfig.get();

                    if (!shouldShowHud(minecraft, config)) {
                        return;
                    }

                    if (config.essenceHudStyle == ModConfig.EssenceHudStyle.FULL) {
                        renderDroplets(graphics, minecraft, essence);
                    } else {
                        renderMinimal(graphics, minecraft, essence);
                    }
                }
        );
    }

    private static boolean shouldShowHud(Minecraft minecraft, ModConfig config) {
        return switch (config.essenceHudVisibility) {
            case ALWAYS -> true;

            case CARD_IN_HAND ->
                    minecraft.player.getMainHandItem().is(ModItems.CARD)
                            || minecraft.player.getOffhandItem().is(ModItems.CARD);

            case CARD_IN_INVENTORY -> {
                boolean found = false;

                for (int i = 0; i < minecraft.player.getInventory().getContainerSize(); i++) {
                    if (minecraft.player.getInventory().getItem(i).is(ModItems.CARD)) {
                        found = true;
                        break;
                    }
                }

                yield found;
            }
        };
    }

    private static void renderDroplets(
            GuiGraphicsExtractor graphics,
            Minecraft minecraft,
            int essence
    ) {
        // Right-aligned above the hunger bar.
        int rightX = graphics.guiWidth() / 2 + 91;
        int x = rightX - (DROP_COUNT - 1) * DROP_SPACING - DROP_SIZE;

        // Above the hunger icons.
        int y = graphics.guiHeight() - 49;

        // Move up when the vanilla air meter is present.
        if (minecraft.player.getAirSupply()
                < minecraft.player.getMaxAirSupply()) {
            y -= 10;
        }

        for (int i = 0; i < DROP_COUNT; i++) {
            int remaining = essence - i * 2;

            Identifier texture;

            if (remaining >= 2) {
                texture = DROP_FULL;
            } else if (remaining == 1) {
                texture = DROP_HALF;
            } else {
                texture = DROP_EMPTY;
            }

            drawDrop(
                    graphics,
                    texture,
                    x + i * DROP_SPACING,
                    y
            );
        }
    }

    private static void renderMinimal(
            GuiGraphicsExtractor graphics,
            Minecraft minecraft,
            int essence
    ) {
        int x = graphics.guiWidth() / 2 + 96;
        int y = graphics.guiHeight() - 15;

        drawDrop(graphics, DROP_FULL, x, y);

        graphics.pose().pushMatrix();

        graphics.pose().translate(
                x + DROP_SIZE + 3,
                y + 2
        );

        graphics.pose().scale(0.8F, 0.8F);

        graphics.text(
                minecraft.font,
                essence + "/" + PlayerEssence.MAX_ESSENCE,
                0,
                0,
                0xFFFFFFFF,
                true
        );

        graphics.pose().popMatrix();
    }

    private static void drawDrop(
            GuiGraphicsExtractor graphics,
            Identifier texture,
            int x,
            int y
    ) {
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                texture,
                x, y,
                0.0F, 0.0F,
                DROP_SIZE, DROP_SIZE,
                DROP_SIZE, DROP_SIZE,
                DROP_SIZE, DROP_SIZE
        );
    }
}