package org.labcabrera.parking.bff.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.labcabrera.parking.bff.generated.client.ecommerce.api.OrdersApi;
import org.labcabrera.parking.bff.generated.client.facilities.api.ParkingFacilitiesApi;
import org.labcabrera.parking.bff.generated.client.facilities.api.ReservationsApi;
import org.labcabrera.parking.bff.generated.client.facilities.model.Reservation;
import org.labcabrera.parking.bff.generated.client.facilities.model.StartReservationRequest;
import org.labcabrera.parking.bff.interfaces.rest.dto.CheckoutDto;
import org.labcabrera.parking.bff.interfaces.rest.dto.SelectOptionRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

class CheckoutControllerTest {

    private final ParkingFacilitiesApi parkingFacilitiesApi = mock(ParkingFacilitiesApi.class);
    private final ReservationsApi reservationsApi = mock(ReservationsApi.class);
    private final OrdersApi ordersApi = mock(OrdersApi.class);

    private final CheckoutController controller = new CheckoutController(
        parkingFacilitiesApi,
        reservationsApi,
        ordersApi);

    @Test
    void selectOptionRefreshesReservationUntilPriceIsAvailable() {
        UUID checkoutId = UUID.randomUUID();
        UUID facilityId = UUID.randomUUID();
        LocalDateTime checkIn = LocalDateTime.of(2026, 6, 7, 10, 0);
        LocalDateTime checkOut = LocalDateTime.of(2026, 6, 7, 12, 0);

        Reservation pending = reservation(checkoutId, facilityId, checkIn, checkOut)
            .status("PENDING");
        Reservation held = reservation(checkoutId, facilityId, checkIn, checkOut)
            .status("HELD")
            .estimatedPrice(new BigDecimal("12.50"))
            .currency("EUR");

        ReflectionTestUtils.setField(controller, "reservationRefreshAttempts", 1);
        ReflectionTestUtils.setField(controller, "reservationRefreshDelayMs", 0L);
        when(reservationsApi.startWithHttpInfo(any(StartReservationRequest.class))).thenReturn(ResponseEntity.ok(pending));
        when(reservationsApi.getWithHttpInfo(checkoutId)).thenReturn(ResponseEntity.ok(held));

        ResponseEntity<CheckoutDto> response = controller.selectOption(
            checkoutId,
            new SelectOptionRequest(facilityId, checkIn, checkOut));

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().amount()).isEqualByComparingTo("12.50");
        assertThat(response.getBody().currency()).isEqualTo("EUR");
        assertThat(response.getBody().status()).isEqualTo("HELD");
        verify(reservationsApi).getWithHttpInfo(checkoutId);
    }

    private Reservation reservation(UUID reservationId, UUID facilityId, LocalDateTime checkIn, LocalDateTime checkOut) {
        return new Reservation()
            .id(reservationId)
            .facilityId(facilityId)
            .userId("user-test")
            .checkIn(checkIn)
            .checkOut(checkOut);
    }
}
