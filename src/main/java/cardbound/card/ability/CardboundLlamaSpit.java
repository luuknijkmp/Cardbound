package cardbound.card.ability;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LlamaSpit;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class CardboundLlamaSpit extends LlamaSpit {

    public CardboundLlamaSpit(ServerLevel level, Player owner) {
        super(EntityTypes.LLAMA_SPIT, level);
        this.setOwner(owner);
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        super.onHitEntity(hitResult);

        Entity target = hitResult.getEntity();

        if (!(target instanceof LivingEntity livingTarget)) {
            return;
        }

        Vec3 direction = livingTarget.position()
                .subtract(this.position());

        Vec3 horizontal = new Vec3(
                direction.x,
                0.0,
                direction.z
        );

        if (horizontal.lengthSqr() < 0.0001) {
            horizontal = this.getDeltaMovement()
                    .multiply(1.0, 0.0, 1.0);
        }

        if (horizontal.lengthSqr() < 0.0001) {
            return;
        }

        horizontal = horizontal.normalize();

        livingTarget.push(
                horizontal.x * 2.0,
                0.4,
                horizontal.z * 2.0
        );

        livingTarget.hurtMarked = true;
    }
}