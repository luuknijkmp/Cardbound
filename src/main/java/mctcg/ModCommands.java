
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
import mctcg.essence.PlayerEssence;

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
                                                    .then(Commands.literal("holo")
                                                            .executes(context -> {
                                                                Identifier cardId = IdentifierArgument.getId(
                                                                        context, "card_id"
                                                                );

                                                                if (!CardRegistry.exists(cardId)) {
                                                                    context.getSource().sendFailure(
                                                                            Component.literal("Unknown card: " + cardId)
                                                                    );
                                                                    return 0;
                                                                }

                                                                ItemStack card = CardFactory.createCard(
                                                                        cardId,
                                                                        CardOrigin.ADMIN_COMMAND,
                                                                        true
                                                                );

                                                                context.getSource().getPlayerOrException()
                                                                        .getInventory().add(card);

                                                                return 1;
                                                            })
                                                    )
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

                    dispatcher.register(
                            Commands.literal("essence")
                                    .executes(context -> {
                                        var player = context.getSource().getPlayerOrException();

                                        int essence = player.getAttachedOrCreate(PlayerEssence.ESSENCE);

                                        context.getSource().sendSuccess(
                                                () -> Component.literal(
                                                        "Essence: " + essence + "/" + PlayerEssence.MAX_ESSENCE
                                                ),
                                                false
                                        );

                                        return Command.SINGLE_SUCCESS;
                                    })
                                    .then(
                                            Commands.literal("spend")
                                                    .requires(source -> source.permissions()
                                                            .hasPermission(Permissions.COMMANDS_GAMEMASTER))
                                                    .executes(context -> {
                                                        var player = context.getSource().getPlayerOrException();

                                                        int current = player.getAttachedOrCreate(PlayerEssence.ESSENCE);
                                                        int newAmount = Math.max(0, current - 5);

                                                        player.setAttached(PlayerEssence.ESSENCE, newAmount);

                                                        context.getSource().sendSuccess(
                                                                () -> Component.literal(
                                                                        "Spent 5 Essence. Remaining: "
                                                                                + newAmount + "/"
                                                                                + PlayerEssence.MAX_ESSENCE
                                                                ),
                                                                false
                                                        );

                                                        return Command.SINGLE_SUCCESS;
                                                    })
                                    )
                                    .then(
                                            Commands.literal("max")
                                                    .requires(source -> source.permissions()
                                                            .hasPermission(Permissions.COMMANDS_GAMEMASTER))
                                                    .executes(context -> {
                                                        var player = context.getSource().getPlayerOrException();

                                                        player.setAttached(
                                                                PlayerEssence.ESSENCE,
                                                                PlayerEssence.MAX_ESSENCE
                                                        );

                                                        context.getSource().sendSuccess(
                                                                () -> Component.literal(
                                                                        "Essence refilled: "
                                                                                + PlayerEssence.MAX_ESSENCE + "/"
                                                                                + PlayerEssence.MAX_ESSENCE
                                                                ),
                                                                false
                                                        );

                                                        return Command.SINGLE_SUCCESS;
                                                    })
                                    )
                    );

                }
        );


    }
}
