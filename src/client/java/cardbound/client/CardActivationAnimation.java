package cardbound.client;

import net.minecraft.world.item.ItemStack;

public final class CardActivationAnimation {

    private static final long DURATION_MS = 400L;

    private static ItemStack activeCard = ItemStack.EMPTY;
    private static long startTime;

    private CardActivationAnimation() {
    }

    public static void start(ItemStack card) {
        activeCard = card.copy();
        startTime = System.currentTimeMillis();
    }

    public static boolean isActive() {
        return !activeCard.isEmpty()
                && System.currentTimeMillis() - startTime < DURATION_MS;
    }

    public static float progress() {
        return Math.min(
                1.0F,
                (System.currentTimeMillis() - startTime) / (float) DURATION_MS
        );
    }

    public static ItemStack getCard() {
        return activeCard;
    }
}