package cardbound.card.ability;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class CardboundSnowball extends Snowball {

    private final float knockbackStrength;

    public CardboundSnowball(
            ServerLevel level,
            LivingEntity owner,
            float knockbackStrength
    ) {
        super(EntityTypes.SNOWBALL, level);

        this.knockbackStrength = knockbackStrength;
        this.setOwner(owner);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!(result.getEntity() instanceof LivingEntity target)) {
            super.onHitEntity(result);
            return;
        }

        // Don't hit the caster.
        if (target == this.getOwner()) {
            return;
        }

        // Don't hit any Cardbound summoned allies.
        if (target instanceof SummonedAlly ally
                && ally.cardbound$getOwnerUuid() != null) {
            return;
        }

        // Let vanilla snowball collision behavior run.
        super.onHitEntity(result);

        Vec3 movement = this.getDeltaMovement();
        Vec3 horizontal = new Vec3(movement.x, 0.0, movement.z);

        if (horizontal.lengthSqr() < 0.0001) {
            return;
        }

        horizontal = horizontal.normalize();

        target.push(
                horizontal.x * knockbackStrength,
                0.25,
                horizontal.z * knockbackStrength
        );

        target.hurtMarked = true;
    }
}