package cardbound.card.ability;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.StonecutterMenu;
import net.minecraft.network.chat.Component;

public final class PortableMenuHelper {

    private PortableMenuHelper() {
    }

    public static boolean open(ServerPlayer player, String menu) {
        BlockPos pos = player.blockPosition();

        ContainerLevelAccess access =
                ContainerLevelAccess.create(player.level(), pos);

        MenuProvider provider;

        if (menu.equals("crafting")) {
            provider = new MenuProvider() {

                @Override
                public Component getDisplayName() {
                    return Component.translatable("container.crafting");
                }

                @Override
                public AbstractContainerMenu createMenu(
                        int containerId,
                        Inventory inventory,
                        Player player
                ) {
                    return new CraftingMenu(
                            containerId,
                            inventory,
                            access
                    ) {
                        @Override
                        public boolean stillValid(Player player) {
                            return true;
                        }
                    };
                }
            };

        } else if (menu.equals("stonecutter")) {
            provider = new MenuProvider() {

                @Override
                public Component getDisplayName() {
                    return Component.translatable("container.stonecutter");
                }

                @Override
                public AbstractContainerMenu createMenu(
                        int containerId,
                        Inventory inventory,
                        Player player
                ) {
                    return new StonecutterMenu(
                            containerId,
                            inventory,
                            access
                    ) {
                        @Override
                        public boolean stillValid(Player player) {
                            return true;
                        }
                    };
                }
            };

        } else {
            return false;
        }

        return player.openMenu(provider).isPresent();
    }
}