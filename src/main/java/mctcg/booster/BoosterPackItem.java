package mctcg.booster;

import mctcg.ModComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class BoosterPackItem extends Item {

    public BoosterPackItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(
            Level level,
            Player player,
            InteractionHand hand
    ) {
        ItemStack stack = player.getItemInHand(hand);

        // Only the server should generate rewards.
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        var boosterId = stack.get(ModComponents.PACK_ID);

        // An unidentified booster cannot be opened.
        if (boosterId == null || !BoosterRegistry.exists(boosterId)) {
            return InteractionResult.FAIL;
        }

        var rewards = BoosterPackFactory.openPack(
                boosterId,
                player.getRandom()
        );

        // Don't consume the booster if no cards were generated.
        if (rewards.isEmpty()) {
            return InteractionResult.FAIL;
        }

        // Give the generated cards to the player.
        for (ItemStack reward : rewards) {
            if (!player.getInventory().add(reward)) {
                player.drop(reward, false);
            }
        }

        // Consume one booster, except in Creative mode.
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        return InteractionResult.SUCCESS;
    }
}