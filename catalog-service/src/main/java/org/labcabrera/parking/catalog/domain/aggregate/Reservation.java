package org.labcabrera.parking.catalog.domain.aggregate;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import lombok.Getter;

@Getter
public class Reservation {

    private UUID id;
    private Optional<String> parkingFacilityId;
    private String userId;
    private String checkIn;
    private String checkOut;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

    public static Reservation create(String parkingFacilityId, String userId, String checkIn, String checkOut,
        int expirationInSeconds) {
        return new Reservation(parkingFacilityId, userId, checkIn, checkOut, expirationInSeconds);
    }

    private Reservation(String parkingFacilityId, String userId, String checkIn, String checkOut, int expirationInSeconds) {
        this.id = UUID.randomUUID();
        this.parkingFacilityId = Optional.of(parkingFacilityId);
        this.userId = userId;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusSeconds(expirationInSeconds);
    }

}
