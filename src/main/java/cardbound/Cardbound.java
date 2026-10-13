package cardbound;

import cardbound.booster.BoosterReloadListener;
import cardbound.card.ability.CardEffects;
import cardbound.essence.PlayerEssence;
import cardbound.network.CardRevealPayload;
import cardbound.set.SetReloadListener;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.resource.v1.reloader.ResourceReloaderKeys;
import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cardbound.card.CardReloadListener;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.server.packs.PackType;
import cardbound.network.CardActivationPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class Cardbound implements ModInitializer {
	public static final String MOD_ID = "cardbound";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		ModItems.initialize();
		// ModMobDrops.initialize(); <-- UNCOMMENT TO ENABLE VAILS OF ESSENCE IN MOB DROPS
		ModChestLoot.initialize();
		ModComponents.initialize();
		ModCommands.initialize();
		PlayerEssence.initialize();
		ModSounds.initialize();
		ModCreativeTabs.initialize();
		CardEffects.initialize();

		//System.out.println("[CARDBOUND] Registering card reload listener");
		ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(
				Identifier.fromNamespaceAndPath("cardbound", "cards"),
				new CardReloadListener()
		);

		ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(
				Identifier.fromNamespaceAndPath("cardbound", "sets"),
				new SetReloadListener()
		);

		ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(
				Identifier.fromNamespaceAndPath("cardbound", "boosters"),
				new BoosterReloadListener()
		);

		PayloadTypeRegistry.clientboundPlay().register(
				CardActivationPayload.TYPE,
				CardActivationPayload.CODEC
		);

		PayloadTypeRegistry.clientboundPlay().register(
				CardRevealPayload.TYPE,
				CardRevealPayload.CODEC
		);

		var serverDataLoader = ResourceLoader.get(PackType.SERVER_DATA);

		Identifier chestLootListenerId = Identifier.fromNamespaceAndPath(
				"cardbound",
				"chest_loot"
		);

		serverDataLoader.registerReloadListener(
				chestLootListenerId,
				new ChestLootReloadListener()
		);


	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
