package cardbound.card.ability;

import java.util.UUID;

public interface SummonedAlly {

    UUID cardbound$getOwnerUuid();

    void cardbound$setOwnerUuid(UUID ownerUuid);

    long cardbound$getExpirationTick();

    void cardbound$setExpirationTick(long expirationTick);
}