package cardbound.card.ability;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;

import java.util.UUID;

public class SilverfishAllyTargetGoal
        extends NearestAttackableTargetGoal<LivingEntity> {

    public SilverfishAllyTargetGoal(Mob mob) {
        super(
                mob,
                LivingEntity.class,
                10,
                true,
                false,
                (target, level) -> {
                    if (!target.isAlive() || target == mob) {
                        return false;
                    }

                    if (!(mob instanceof SummonedAlly ally)) {
                        return false;
                    }

                    UUID ownerUuid = ally.cardbound$getOwnerUuid();

                    // Only summoned silverfish use this targeting.
                    if (ownerUuid == null) {
                        return false;
                    }

                    // Never attack the summoner.
                    if (target.getUUID().equals(ownerUuid)) {
                        return false;
                    }

                    // Never attack other Cardbound summoned allies.
                    if (target instanceof SummonedAlly targetAlly
                            && targetAlly.cardbound$getOwnerUuid() != null) {
                        return false;
                    }

                    // Everything else is fair game!
                    return true;
                }
        );
    }
}