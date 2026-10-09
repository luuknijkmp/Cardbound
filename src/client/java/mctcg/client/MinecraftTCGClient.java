package mctcg.client;

import net.fabricmc.api.ClientModInitializer;
import mctcg.network.CardActivationPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import mctcg.ModSounds;
import net.minecraft.sounds.SoundSource;

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

					float pitch = 0.95F + minecraft.player.getRandom().nextFloat() * 0.10F;

					minecraft.player.playSound(
							ModSounds.CARD_ACTIVATE,
							1.0F,
							pitch
					);
				}
		);
	}
}