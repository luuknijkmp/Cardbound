package cardbound.card.ability;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;

public class AllyTargetGoal extends NearestAttackableTargetGoal<LivingEntity> {

    public AllyTargetGoal(Mob mob) {
        super(
                mob,
                LivingEntity.class,
                10,
                true,
                false,
                (target, level) -> target instanceof Enemy
                        && target != mob
                        && target.isAlive()
                        && !(target instanceof SummonedAlly ally
                        && ally.cardbound$getOwnerUuid() != null)
        );
    }
}