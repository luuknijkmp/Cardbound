package cardbound.client;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import cardbound.client.config.ModConfig;
import cardbound.network.CardRevealPayload;
import net.fabricmc.api.ClientModInitializer;
import cardbound.network.CardActivationPayload;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import cardbound.ModSounds;

public class CardboundClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ModConfig.load();
		EssenceHud.initialize();
		ModKeybindings.initialize();

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

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildContext) -> {
			dispatcher.register(
					LiteralArgumentBuilder.<FabricClientCommandSource>literal("cardreveal")
							.executes(context -> {
								Minecraft minecraft = Minecraft.getInstance();

								minecraft.execute(() ->
										minecraft.gui.setScreen(new CardRevealScreen(minecraft.player.getMainHandItem()))
								);

								return 1;
							})
			);
		});

		ClientPlayNetworking.registerGlobalReceiver(
				CardRevealPayload.TYPE,
				(payload, context) -> {
					Minecraft minecraft = Minecraft.getInstance();

					minecraft.execute(() -> {
						if (minecraft.player != null
								&& (payload.manual() || ModConfig.get().showCardReveal)) {

							minecraft.gui.setScreen(
									new CardRevealScreen(payload.card())
							);
						}
					});
				}
		);

	}
}