package org.labcabrera.parking.facilities.domain.service;

public class AvailabilityDomainService {

    private static final double LOW_AVAILABILITY_THRESHOLD = 0.2;

    public boolean hasAvailability(int availableSpots) {
        return availableSpots > 0;
    }

    public boolean isLowAvailability(int available, int total) {
        if (total <= 0) {
            return false;
        }
        return available <= total * LOW_AVAILABILITY_THRESHOLD;
    }
}
