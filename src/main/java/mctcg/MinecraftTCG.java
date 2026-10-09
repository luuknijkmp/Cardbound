package mctcg;

import mctcg.booster.BoosterReloadListener;
import mctcg.essence.PlayerEssence;
import mctcg.set.SetReloadListener;
import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mctcg.card.CardReloadListener;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;

public class MinecraftTCG implements ModInitializer {
	public static final String MOD_ID = "minecraft-tcg";

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
		ModComponents.initialize();
		ModCommands.initialize();
		PlayerEssence.initialize();

		//System.out.println("[MCTCG] Registering card reload listener");
		ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(
				Identifier.fromNamespaceAndPath("mctcg", "cards"),
				new CardReloadListener()
		);

		ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(
				Identifier.fromNamespaceAndPath("mctcg", "sets"),
				new SetReloadListener()
		);

		ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(
				Identifier.fromNamespaceAndPath("mctcg", "boosters"),
				new BoosterReloadListener()
		);
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
