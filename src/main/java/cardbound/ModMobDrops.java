package cardbound;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;

public class ModMobDrops {

    private static final float VIAL_DROP_CHANCE = 0.005f;

    public static void initialize() {

        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {

            if (!source.isBuiltin()) {
                return;
            }

            // Find any hostile entity whose loot table matches this one.
            boolean isHostileMob = BuiltInRegistries.ENTITY_TYPE.stream()
                    .anyMatch(entityType ->
                            entityType.getCategory() == MobCategory.MONSTER
                                    && entityType.getDefaultLootTable()
                                    .filter(key::equals)
                                    .isPresent()
                    );

            if (!isHostileMob) {
                return;
            }

            LootPool.Builder vialPool = LootPool.lootPool()
                    .add(LootItem.lootTableItem(ModItems.VIAL_OF_ESSENCE))
                    .when(LootItemKilledByPlayerCondition.killedByPlayer())
                    .when(LootItemRandomChanceCondition.randomChance(VIAL_DROP_CHANCE));

            tableBuilder.withPool(vialPool);
        });
    }
}