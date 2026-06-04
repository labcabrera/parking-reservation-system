package org.labcabrera.parking.reservation.domain.port.outbound;

import org.labcabrera.parking.reservation.domain.model.HoldReadModel;
import org.labcabrera.parking.reservation.domain.model.HoldStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HoldRepository {

    Optional<HoldReadModel> findById(UUID holdId);

    Optional<HoldReadModel> findActiveBySessionAndFacilityAndPeriod(
            String searchSessionId, UUID facilityId, Instant checkIn, Instant checkOut);

    List<HoldReadModel> findByStatusAndExpiresAtBefore(HoldStatus status, Instant threshold);

    HoldReadModel save(HoldReadModel holdReadModel);
}
