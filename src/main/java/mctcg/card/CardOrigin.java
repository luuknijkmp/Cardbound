package mctcg.card;

import net.minecraft.ChatFormatting;

public enum CardOrigin {

    BOOSTER_PACK("Booster Pack", ChatFormatting.GREEN),
    ADMIN_COMMAND("Admin Command", ChatFormatting.RED),
    EVENT_REWARD("Event Reward", ChatFormatting.AQUA),
    UNKNOWN("Unknown", ChatFormatting.GRAY);

    private final String displayName;
    private final ChatFormatting color;

    CardOrigin(String displayName, ChatFormatting color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ChatFormatting getColor() {
        return color;
    }
}