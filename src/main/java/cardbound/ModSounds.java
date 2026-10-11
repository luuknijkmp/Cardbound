package cardbound;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public class ModSounds {

    public static final Identifier CARD_ACTIVATE_ID =
            Identifier.fromNamespaceAndPath("cardbound", "card_activate");

    public static final SoundEvent CARD_ACTIVATE = Registry.register(
            BuiltInRegistries.SOUND_EVENT,
            CARD_ACTIVATE_ID,
            SoundEvent.createVariableRangeEvent(CARD_ACTIVATE_ID)
    );

    public static final Identifier CARD_UNPACKED_ID =
            Identifier.fromNamespaceAndPath("cardbound", "card_unpacked");

    public static final SoundEvent CARD_UNPACKED = Registry.register(
            BuiltInRegistries.SOUND_EVENT,
            CARD_UNPACKED_ID,
            SoundEvent.createVariableRangeEvent(CARD_UNPACKED_ID)
    );

    public static void initialize() {
        // Calling this ensures the class is loaded and the sound is registered.
    }
}