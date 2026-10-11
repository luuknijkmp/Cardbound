package mctcg.client.gui;

import mctcg.client.config.ModConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.network.chat.Component;

public class ModSettingsScreen extends Screen {

    private final Screen parent;

    public ModSettingsScreen(Screen parent) {
        super(Component.literal("McTCG Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;

        // Two-column layout, matching Mod Menu's style.
        int buttonWidth = 140;
        int buttonHeight = 20;
        int gap = 10;

        int leftX = centerX - buttonWidth - gap / 2;
        int rightX = centerX + gap / 2;

        // Settings start near the top of the screen.
        int startY = 45;

        this.addRenderableWidget(
                Button.builder(
                        getStyleLabel(),
                        button -> {
                            ModConfig config = ModConfig.get();

                            config.essenceHudStyle =
                                    config.essenceHudStyle == ModConfig.EssenceHudStyle.FULL
                                            ? ModConfig.EssenceHudStyle.MINIMAL
                                            : ModConfig.EssenceHudStyle.FULL;

                            ModConfig.save();
                            button.setMessage(getStyleLabel());
                        }
                ).bounds(leftX, startY, buttonWidth, buttonHeight).build()
        );

        this.addRenderableWidget(
                Button.builder(
                        getVisibilityLabel(),
                        button -> {
                            ModConfig config = ModConfig.get();

                            config.essenceHudVisibility = switch (config.essenceHudVisibility) {
                                case ALWAYS ->
                                        ModConfig.EssenceHudVisibility.CARD_IN_INVENTORY;
                                case CARD_IN_INVENTORY ->
                                        ModConfig.EssenceHudVisibility.CARD_IN_HAND;
                                case CARD_IN_HAND ->
                                        ModConfig.EssenceHudVisibility.ALWAYS;
                            };

                            ModConfig.save();
                            button.setMessage(getVisibilityLabel());
                        }
                ).bounds(rightX, startY, buttonWidth, buttonHeight).build()
        );

        // Done button anchored near the bottom.
        this.addRenderableWidget(
                Button.builder(
                        Component.literal("Done"),
                        button -> onClose()
                ).bounds(centerX - 100, this.height - 27, 200, 20).build()
        );

        // Open Minecraft's built-in keybinding settings.
        this.addRenderableWidget(
                Button.builder(
                        Component.literal("Configure Keybinds..."),
                        button -> this.minecraft.gui.setScreen(
                                new KeyBindsScreen(this, this.minecraft.options)
                        )
                ).bounds(leftX, startY + 28, buttonWidth, buttonHeight).build()
        );

        // Enable or disable automatic card reveal screens.
        this.addRenderableWidget(
                Button.builder(
                        getCardRevealLabel(),
                        button -> {
                            ModConfig config = ModConfig.get();

                            config.showCardReveal = !config.showCardReveal;

                            ModConfig.save();
                            button.setMessage(getCardRevealLabel());
                        }
                ).bounds(rightX, startY + 28, buttonWidth, buttonHeight).build()
        );
    }

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        // Centered title at the top.
        String title = "McTCG Settings";
        graphics.text(
                this.font,
                title,
                (this.width - this.font.width(title)) / 2,
                15,
                0xFFFFFFFF,
                true
        );
    }

    private Component getStyleLabel() {
        String style = switch (ModConfig.get().essenceHudStyle) {
            case FULL -> "Full";
            case MINIMAL -> "Minimal";
        };

        return Component.literal("Essence HUD Style: " + style);
    }

    private Component getVisibilityLabel() {
        String visibility = switch (ModConfig.get().essenceHudVisibility) {
            case ALWAYS -> "Always";
            case CARD_IN_INVENTORY -> "Card in Inventory";
            case CARD_IN_HAND -> "Card in Hand";
        };

        return Component.literal("Essence HUD Visibility: " + visibility);
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(parent);
    }

    private Component getCardRevealLabel() {
        return Component.literal(
                "Card Reveal: " +
                        (ModConfig.get().showCardReveal ? "Enabled" : "Disabled")
        );
    }
}