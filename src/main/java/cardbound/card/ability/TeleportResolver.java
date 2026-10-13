package cardbound.card.ability;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.ClipContext;
import java.util.Optional;
import net.minecraft.world.phys.Vec3;

public final class TeleportResolver {

    private TeleportResolver() {
    }

    public static boolean isSafeDestination(
            ServerLevel level,
            Player player,
            Vec3 destination
    ) {
        BlockPos feet = BlockPos.containing(destination);
        BlockPos below = feet.below();

        // Never teleport into an unloaded chunk.
        if (!level.hasChunkAt(feet)) {
            return false;
        }

        // Require solid ground beneath the player.
        if (!level.getBlockState(below).isFaceSturdy(
                level,
                below,
                net.minecraft.core.Direction.UP
        )) {
            return false;
        }

        // Avoid dangerous landing blocks.
        if (level.getBlockState(feet).is(Blocks.LAVA)
                || level.getBlockState(below).is(Blocks.MAGMA_BLOCK)
                || level.getBlockState(below).is(Blocks.CAMPFIRE)
                || level.getBlockState(below).is(Blocks.SOUL_CAMPFIRE)) {
            return false;
        }

        // Check the player's entire bounding box, not just their feet.
        AABB destinationBox = player.getBoundingBox().move(
                destination.x - player.getX(),
                destination.y - player.getY(),
                destination.z - player.getZ()
        );

        return level.noCollision(player, destinationBox);
    }

    public static Optional<Vec3> findForwardDestination(
            ServerLevel level,
            Player player,
            float distance,
            boolean throughWalls
    ) {
        Vec3 start = player.position();

        // Horizontal direction only: looking up/down won't change the teleport.
        Vec3 direction = player.getLookAngle();
        direction = new Vec3(direction.x, 0.0, direction.z);

        if (direction.lengthSqr() < 0.0001) {
            return Optional.empty();
        }

        direction = direction.normalize();

        double maximumDistance = distance;

        if (!throughWalls) {
            Vec3 eyeStart = player.getEyePosition();
            Vec3 eyeEnd = eyeStart.add(direction.scale(distance));

            BlockHitResult hit = level.clip(new ClipContext(
                    eyeStart,
                    eyeEnd,
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE,
                    player
            ));

            if (hit.getType() != HitResult.Type.MISS) {
                maximumDistance = Math.max(
                        0.0,
                        hit.getLocation().distanceTo(eyeStart) - 0.5
                );
            }
        }

        // Search from the farthest position back toward the player.
        // Stops at the first safe landing spot.
        double[] heightOffsets = {
                0.0,
                1.0,
                -1.0,
                2.0,
                -2.0
        };

        for (double d = maximumDistance; d >= 0.5; d -= 0.25) {
            Vec3 horizontalPosition = start.add(direction.scale(d));

            for (double heightOffset : heightOffsets) {
                Vec3 candidate = horizontalPosition.add(
                        0.0,
                        heightOffset,
                        0.0
                );

                if (!isSafeDestination(level, player, candidate)) {
                    continue;
                }

                // Without wall traversal, the entire teleport path
                // must remain unobstructed.
                if (!throughWalls) {
                    Vec3 startCenter = player.getBoundingBox().getCenter();

                    Vec3 endCenter = startCenter.add(
                            candidate.subtract(start)
                    );

                    BlockHitResult pathHit = level.clip(new ClipContext(
                            startCenter,
                            endCenter,
                            ClipContext.Block.COLLIDER,
                            ClipContext.Fluid.NONE,
                            player
                    ));

                    if (pathHit.getType() != HitResult.Type.MISS) {
                        continue;
                    }
                }

                return Optional.of(candidate);
            }
        }

        return Optional.empty();
    }

    public static Optional<Vec3> findToTargetDestination(
            ServerLevel level,
            Player player,
            float distance,
            boolean throughWalls
    ) {
        Optional<LivingEntity> targetResult = TargetResolver.resolve(
                player,
                AbilityTarget.LOOKED_AT,
                40.0F
        );

        if (targetResult.isEmpty()) {
            return Optional.empty();
        }

        LivingEntity target = targetResult.get();

        // Approach from the player's side of the target.
        Vec3 direction = new Vec3(
                player.getX() - target.getX(),
                0.0,
                player.getZ() - target.getZ()
        );

        if (direction.lengthSqr() < 0.0001) {
            Vec3 look = target.getLookAngle();
            direction = new Vec3(look.x, 0.0, look.z);
        }

        if (direction.lengthSqr() < 0.0001) {
            return Optional.empty();
        }

        direction = direction.normalize();

        double[] heightOffsets = {0.0, 1.0, -1.0, 2.0, -2.0};

        // Start at the preferred distance, then search nearby.
        for (double d = distance; d >= 0.5; d -= 0.25) {
            Vec3 horizontalPosition = target.position().add(
                    direction.scale(d)
            );

            for (double heightOffset : heightOffsets) {
                Vec3 candidate = horizontalPosition.add(
                        0.0,
                        heightOffset,
                        0.0
                );

                if (!isSafeDestination(level, player, candidate)) {
                    continue;
                }

                if (!throughWalls) {
                    Vec3 start = player.getBoundingBox().getCenter();
                    Vec3 end = start.add(candidate.subtract(player.position()));

                    BlockHitResult hit = level.clip(new ClipContext(
                            start,
                            end,
                            ClipContext.Block.COLLIDER,
                            ClipContext.Fluid.NONE,
                            player
                    ));

                    if (hit.getType() != HitResult.Type.MISS) {
                        continue;
                    }
                }

                return Optional.of(candidate);
            }
        }

        return Optional.empty();
    }

    public static Optional<Vec3> findBehindTargetDestination(
            ServerLevel level,
            Player player,
            float distance,
            boolean throughWalls
    ) {
        // Find the living entity we're looking at.
        Optional<LivingEntity> targetResult = TargetResolver.resolve(
                player,
                AbilityTarget.LOOKED_AT,
                16.0F
        );

        if (targetResult.isEmpty()) {
            return Optional.empty();
        }

        LivingEntity target = targetResult.get();

        // Use the direction the target is facing.
        Vec3 look = target.getLookAngle();
        Vec3 behind = new Vec3(-look.x, 0.0, -look.z);

        if (behind.lengthSqr() < 0.0001) {
            return Optional.empty();
        }

        behind = behind.normalize();

        // Search behind the target, starting at the desired distance.
        for (double d = distance; d >= 0.5; d -= 0.25) {
            Vec3 horizontalPosition = target.position().add(behind.scale(d));

            double[] heightOffsets = {0.0, 1.0, -1.0, 2.0, -2.0};

            for (double heightOffset : heightOffsets) {
                Vec3 candidate = horizontalPosition.add(0.0, heightOffset, 0.0);

                if (!isSafeDestination(level, player, candidate)) {
                    continue;
                }

                if (!throughWalls) {
                    Vec3 start = player.getBoundingBox().getCenter();
                    Vec3 end = start.add(candidate.subtract(player.position()));

                    BlockHitResult hit = level.clip(new ClipContext(
                            start,
                            end,
                            ClipContext.Block.COLLIDER,
                            ClipContext.Fluid.NONE,
                            player
                    ));

                    if (hit.getType() != HitResult.Type.MISS) {
                        continue;
                    }
                }

                return Optional.of(candidate);
            }
        }

        return Optional.empty();
    }

    public static Optional<LivingEntity> findRecentAttacker(Player player) {
        LivingEntity attacker = player.getLastHurtByMob();

        if (attacker == null || !attacker.isAlive()) {
            return Optional.empty();
        }

        int ticksSinceAttack =
                player.tickCount - player.getLastHurtByMobTimestamp();

        // The attacker must have hit us within the last 10 seconds.
        if (ticksSinceAttack < 0 || ticksSinceAttack > 200) {
            return Optional.empty();
        }

        // Don't treat ourselves as our own attacker.
        if (attacker == player) {
            return Optional.empty();
        }

        return Optional.of(attacker);
    }

    public static Optional<Vec3> findEscapeDestination(
            ServerLevel level,
            Player player,
            float distance,
            boolean throughWalls
    ) {
        Optional<LivingEntity> recentAttacker = findRecentAttacker(player);

        if (recentAttacker.isEmpty()) {
            return Optional.empty();
        }

        LivingEntity attacker = recentAttacker.get();

        // Calculate the horizontal direction away from the attacker.
        Vec3 direction = new Vec3(
                player.getX() - attacker.getX(),
                0.0,
                player.getZ() - attacker.getZ()
        );

        // If the attacker occupies almost the same position,
        // escape in the opposite direction the player is facing.
        if (direction.lengthSqr() < 0.0001) {
            Vec3 look = player.getLookAngle();
            direction = new Vec3(-look.x, 0.0, -look.z);
        }

        if (direction.lengthSqr() < 0.0001) {
            return Optional.empty();
        }

        direction = direction.normalize();

        Vec3 start = player.position();

        double[] heightOffsets = {
                0.0,
                1.0,
                -1.0,
                2.0,
                -2.0
        };

        // Try the farthest escape position first.
        for (double d = distance; d >= 0.5; d -= 0.25) {
            Vec3 horizontalPosition = start.add(direction.scale(d));

            for (double heightOffset : heightOffsets) {
                Vec3 candidate = horizontalPosition.add(
                        0.0,
                        heightOffset,
                        0.0
                );

                if (!isSafeDestination(level, player, candidate)) {
                    continue;
                }

                // When wall traversal is disabled, check the path.
                if (!throughWalls) {
                    Vec3 startCenter = player.getBoundingBox().getCenter();
                    Vec3 endCenter = startCenter.add(candidate.subtract(start));

                    BlockHitResult hit = level.clip(new ClipContext(
                            startCenter,
                            endCenter,
                            ClipContext.Block.COLLIDER,
                            ClipContext.Fluid.NONE,
                            player
                    ));

                    if (hit.getType() != HitResult.Type.MISS) {
                        continue;
                    }
                }

                return Optional.of(candidate);
            }
        }

        return Optional.empty();
    }
}