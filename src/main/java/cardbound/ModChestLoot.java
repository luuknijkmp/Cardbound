
package cardbound;

import cardbound.booster.BoosterPackItemFactory;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ModChestLoot {

    public static void initialize() {

        LootTableEvents.MODIFY_DROPS.register((holder, context, drops) -> {

            // Only act on registered loot tables.
            var optionalKey = holder.unwrapKey();

            if (optionalKey.isEmpty()) {
                return;
            }

            Identifier lootTableId = optionalKey.get().identifier();

            // Only tables listed in our JSON receive Cardbound drops.
            ChestLootConfig.ChestRule rule = ChestLootConfig.get(lootTableId);

            if (rule == null) {
                return;
            }

            // Independent chance to receive one Vial of Essence.
            if (ThreadLocalRandom.current().nextFloat() < rule.vialChance()) {
                drops.add(new ItemStack(ModItems.VIAL_OF_ESSENCE));
            }

            // Independent chance to receive one booster pack.
            List<Identifier> boosters = rule.boosters();

            if (!boosters.isEmpty()
                    && ThreadLocalRandom.current().nextFloat() < rule.boosterChance()) {

                int index = ThreadLocalRandom.current().nextInt(boosters.size());
                Identifier boosterId = boosters.get(index);

                ItemStack booster = BoosterPackItemFactory.createPack(boosterId);

                if (!booster.isEmpty()) {
                    drops.add(booster);
                }
            }
        });
    }
}
