
package mctcg;

import com.mojang.brigadier.Command;
import mctcg.booster.BoosterPackItemFactory;
import mctcg.booster.BoosterRegistry;
import mctcg.card.CardFactory;
import mctcg.card.CardRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.resources.Identifier;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import mctcg.card.CardOrigin;

public class ModCommands {

    public static void initialize() {
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> {

                    dispatcher.register(
                            Commands.literal("givecard")
                                    .requires(source -> source.permissions()
                                            .hasPermission(Permissions.COMMANDS_GAMEMASTER))
                                    .then(
                                            Commands.argument("card_id", IdentifierArgument.id())
                                                    .executes(context -> {

                                                        Identifier cardId =
                                                                IdentifierArgument.getId(
                                                                        context, "card_id"
                                                                );

                                                        if (!CardRegistry.exists(cardId)) {
                                                            context.getSource().sendFailure(
                                                                    Component.literal("Unknown card: " + cardId)
                                                            );
                                                            return 0;
                                                        }

                                                        var player =
                                                                context.getSource().getPlayerOrException();

                                                        ItemStack card = CardFactory.createCard(
                                                                cardId,
                                                                CardOrigin.ADMIN_COMMAND
                                                        );

                                                        player.getInventory().add(card);

                                                        context.getSource().sendSuccess(
                                                                () -> Component.literal("Gave " + card.getHoverName().getString()
                                                                        + " (" + cardId + ")"),
                                                                false
                                                        );

                                                        return Command.SINGLE_SUCCESS;
                                                    })
                                    )
                    );

                    dispatcher.register(
                            Commands.literal("givebooster")
                                    .requires(source -> source.permissions()
                                            .hasPermission(Permissions.COMMANDS_GAMEMASTER))
                                    .then(Commands.argument("booster_id", IdentifierArgument.id())
                                            .executes(context -> {

                                                Identifier boosterId = IdentifierArgument.getId(
                                                        context,
                                                        "booster_id"
                                                );

                                                if (!BoosterRegistry.exists(boosterId)) {
                                                    context.getSource().sendFailure(
                                                            Component.literal(
                                                                    "Unknown booster: " + boosterId
                                                            )
                                                    );
                                                    return 0;
                                                }

                                                ItemStack pack = BoosterPackItemFactory.createPack(
                                                        boosterId
                                                );

                                                var player = context.getSource().getPlayerOrException();

                                                if (!player.getInventory().add(pack)) {
                                                    player.drop(pack, false);
                                                }

                                                return 1;
                                            }))
                    );
                }
        );


    }
}
