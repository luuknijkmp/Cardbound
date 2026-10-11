package cardbound.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class CardRevealScreen extends Screen {

    // Move the entire layout (card + text) together.
    private static final int LAYOUT_OFFSET_X = 0;
    private static final int LAYOUT_OFFSET_Y = 0;

    // Card size: 16 * 16 = 256 pixels.
    private static final int CARD_SCALE = 16;

    // Space between the card and its information.
    private static final int PANEL_GAP = 16;

    // Fly-in animation settings.
    private static final float ANIMATION_DURATION = 5.0F;
    private static final int ANIMATION_DISTANCE = 300; //DEFAULT: 300

    private float animationTicks = 0.0F;

    private final ItemStack revealedCard;

    public CardRevealScreen(ItemStack card) {
        super(Component.literal("Card Reveal"));
        this.revealedCard = card.copy();
    }

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        // Calculate animation progress (0 = start, 1 = finished).
        float progress = Math.min(
                1.0F,
                (animationTicks + partialTick) / ANIMATION_DURATION
        );

        // Ease-out animation: fast at first, slow at the end.
        float eased = 1.0F - (float) Math.pow(1.0F - progress, 3);

        // Smooth floating-point movement.
        float animationOffsetY = ANIMATION_DISTANCE * (1.0F - eased);

        int cardSize = 16 * CARD_SCALE;

        // Normal card position. No animation offset here!
        int cardX = this.width / 2 - cardSize - 20 + LAYOUT_OFFSET_X;
        int cardY = (this.height - cardSize) / 2 + LAYOUT_OFFSET_Y;

        // Apply the animation to EVERYTHING below.
        graphics.pose().pushMatrix();
        graphics.pose().translate(0.0F, animationOffsetY);

        // ====================================
        // DRAW CARD
        // ====================================

        // Draw the original 32x32 PNG directly at 256x256.
        Identifier modelId = revealedCard.get(DataComponents.ITEM_MODEL);

        if (modelId != null) {
            Identifier textureId = Identifier.fromNamespaceAndPath(
                    modelId.getNamespace(),
                    "textures/item/" + modelId.getPath() + ".png"
            );

            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    textureId,
                    cardX,
                    cardY,
                    0.0F,
                    0.0F,
                    cardSize,  // Display width: 256
                    cardSize,  // Display height: 256
                    32,        // Source width: 32
                    32,        // Source height: 32
                    32,        // Texture width: 32
                    32         // Texture height: 32
            );

        } else {
            // Fallback for cards without custom artwork.
            graphics.pose().pushMatrix();
            graphics.pose().translate(cardX, cardY);
            graphics.pose().scale(CARD_SCALE, CARD_SCALE);
            graphics.item(revealedCard, 0, 0);
            graphics.pose().popMatrix();
        }
        // ====================================
        // DRAW INFORMATION
        // ====================================

        int textX = cardX + cardSize + PANEL_GAP;
        int textY = cardY + 12;

        // Card name at 2x size.
        graphics.pose().pushMatrix();

        graphics.pose().translate(textX, textY);
        graphics.pose().scale(2.0F, 2.0F);

        graphics.text(
                this.font,
                this.revealedCard.getHoverName(),
                0,
                0,
                0xFFFFFFFF,
                true
        );

        graphics.pose().popMatrix();

        // Card lore.
        var lore = this.revealedCard.get(DataComponents.LORE);

        if (lore != null) {
            int lineY = textY + 38;

            for (var line : lore.lines()) {
                graphics.text(
                        this.font,
                        line,
                        textX,
                        lineY,
                        0xFFFFFFFF,
                        false
                );

                lineY += 14;
            }
        }

        // Restore the original transformation.
        // IMPORTANT: This closes the animation's pushMatrix().
        graphics.pose().popMatrix();

        // Subtle hint at the bottom of the screen.
        String hint = "Press Esc or click anywhere to close";

        int hintX = (this.width - this.font.width(hint)) / 2;
        int hintY = this.height - 30;

        graphics.text(
                this.font,
                hint,
                hintX,
                hintY,
                0xFFFFFFFF,
                false
        );
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        this.onClose();
        return true;
    }

    // Advance the animation each game tick.
    @Override
    public void tick() {
        super.tick();

        if (animationTicks < ANIMATION_DURATION) {
            animationTicks++;
        }
    }
}