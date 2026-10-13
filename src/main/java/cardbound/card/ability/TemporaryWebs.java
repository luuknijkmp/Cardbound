package cardbound.card.ability;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class TemporaryWebs {

    private static final Map<ServerLevel, Map<BlockPos, Long>> WEBS =
            new HashMap<>();

    private TemporaryWebs() {
    }

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (var levelEntry : WEBS.entrySet()) {
                ServerLevel level = levelEntry.getKey();
                long currentTick = level.getGameTime();

                Iterator<Map.Entry<BlockPos, Long>> iterator =
                        levelEntry.getValue().entrySet().iterator();

                while (iterator.hasNext()) {
                    var web = iterator.next();

                    if (currentTick < web.getValue()) {
                        continue;
                    }

                    BlockPos pos = web.getKey();

                    // Don't load an unloaded chunk just to remove a web.
                    if (!level.hasChunkAt(pos)) {
                        continue;
                    }

                    // Only remove blocks that are still cobwebs.
                    if (level.getBlockState(pos).is(Blocks.COBWEB)) {
                        level.removeBlock(pos, false);
                    }

                    iterator.remove();
                }
            }
        });
    }

    public static boolean place(
            ServerLevel level,
            BlockPos pos,
            int duration
    ) {
        // Never replace an existing block, including natural cobwebs.
        if (!level.hasChunkAt(pos)
                || !level.getBlockState(pos).isAir()) {
            return false;
        }

        if (!level.setBlockAndUpdate(
                pos,
                Blocks.COBWEB.defaultBlockState()
        )) {
            return false;
        }

        WEBS.computeIfAbsent(level, ignored -> new HashMap<>())
                .put(pos.immutable(), level.getGameTime() + duration);

        return true;
    }
}