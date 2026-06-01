package org.labcabrera.parking.catalog.domain.port.outbound;

/**
 * Port for broadcasting availability updates to subscribed clients.
 */
public interface AvailabilityBroadcastPort {
    void send(String facilityId, Object payload);
}
