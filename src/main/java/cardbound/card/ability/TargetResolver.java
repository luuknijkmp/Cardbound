package cardbound.card.ability;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.ClipContext;

import java.util.Optional;

public final class TargetResolver {

    private TargetResolver() {
    }

    public static Optional<LivingEntity> resolve(
            Player player,
            AbilityTarget target,
            double range
    ) {
        if (target == AbilityTarget.SELF) {
            return Optional.of(player);
        }

        if (target != AbilityTarget.LOOKED_AT
                || !Double.isFinite(range)
                || range <= 0) {
            return Optional.empty();
        }

        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(player.getLookAngle().scale(range));

        // Find the first solid block along the player's line of sight.
        HitResult blockHit = player.level().clip(
                new ClipContext(
                        start,
                        end,
                        ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE,
                        player
                )
        );

        // Don't allow targeting entities behind walls.
        double maxDistanceSquared = blockHit.getType() == HitResult.Type.BLOCK
                ? start.distanceToSqr(blockHit.getLocation())
                : range * range;

        AABB searchArea = player.getBoundingBox()
                .expandTowards(player.getLookAngle().scale(range))
                .inflate(1.0);

        LivingEntity closest = null;
        double closestDistanceSquared = maxDistanceSquared;

        for (LivingEntity entity : player.level().getEntitiesOfClass(
                LivingEntity.class,
                searchArea,
                entity -> entity != player && entity.isAlive()
        )) {
            AABB hitbox = entity.getBoundingBox().inflate(0.1);

            // Handle targets whose hitbox contains the eye position.
            if (hitbox.contains(start)) {
                return Optional.of(entity);
            }

            Optional<Vec3> intersection = hitbox.clip(start, end);

            if (intersection.isEmpty()) {
                continue;
            }

            double distanceSquared = start.distanceToSqr(intersection.get());

            if (distanceSquared < closestDistanceSquared) {
                closestDistanceSquared = distanceSquared;
                closest = entity;
            }
        }

        return Optional.ofNullable(closest);
    }
}