package mctcg.client;

import net.fabricmc.api.ClientModInitializer;
import mctcg.network.CardActivationPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;

public class MinecraftTCGClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(
				CardActivationPayload.TYPE,
				(payload, context) -> {
					Minecraft minecraft = Minecraft.getInstance();

					if (minecraft.player == null) {
						return;
					}

					InteractionHand hand = payload.offhand()
							? InteractionHand.OFF_HAND
							: InteractionHand.MAIN_HAND;

					minecraft.gameRenderer.displayItemActivation(
							minecraft.player.getItemInHand(hand).copy()
					);
				}
		);
	}
}