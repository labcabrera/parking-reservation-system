package org.labcabrera.parking.bff.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.Test;
import org.labcabrera.parking.bff.generated.client.ecommerce.api.OrdersApi;
import org.labcabrera.parking.bff.generated.client.ecommerce.model.PageResponseOrder;
import org.labcabrera.parking.bff.generated.client.facilities.api.ParkingFacilitiesApi;
import org.labcabrera.parking.bff.generated.client.facilities.api.ReservationsApi;
import org.labcabrera.parking.bff.generated.client.facilities.model.PageResponse;
import org.labcabrera.parking.bff.generated.client.facilities.model.Reservation;
import org.labcabrera.parking.bff.generated.client.facilities.model.StartReservationRequest;
import org.labcabrera.parking.bff.interfaces.rest.dto.CheckoutDto;
import org.labcabrera.parking.bff.interfaces.rest.dto.SelectOptionRequest;
import org.labcabrera.parking.bff.interfaces.rest.mapper.CheckoutMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

class CheckoutControllerTest {

    private static final String BOOKING_SESSION_ID = "booking-session-123";

    private final ParkingFacilitiesApi parkingFacilitiesApi = mock(ParkingFacilitiesApi.class);
    private final ReservationsApi reservationsApi = mock(ReservationsApi.class);
    private final OrdersApi ordersApi = mock(OrdersApi.class);
    private final CheckoutMapper checkoutMapper = mock(CheckoutMapper.class);

    private final CheckoutController controller = new CheckoutController(
        parkingFacilitiesApi,
        reservationsApi,
        ordersApi,
        checkoutMapper);

    @Test
    void selectOptionUsesReservationIdAsCheckoutId() {
        UUID checkoutId = UUID.randomUUID();
        UUID facilityId = UUID.randomUUID();
        String userId = "user-123";
        LocalDateTime checkIn = LocalDateTime.of(2026, 6, 7, 10, 0);
        LocalDateTime checkOut = LocalDateTime.of(2026, 6, 7, 12, 0);
        Authentication authentication = mock(Authentication.class);

        Reservation pending = reservation(checkoutId, facilityId, checkIn, checkOut)
            .userId(userId)
            .status("PENDING");
        Reservation held = reservation(checkoutId, facilityId, checkIn, checkOut)
            .userId(userId)
            .status("HELD")
            .estimatedPrice(new BigDecimal("12.50"))
            .currency("EUR");

        ReflectionTestUtils.setField(controller, "reservationRefreshAttempts", 1);
        ReflectionTestUtils.setField(controller, "reservationRefreshDelayMs", 0L);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(userId);
        when(reservationsApi.startWithHttpInfo(any(StartReservationRequest.class))).thenReturn(ResponseEntity.ok(pending));
        when(reservationsApi.getWithHttpInfo(checkoutId)).thenReturn(ResponseEntity.ok(held));
        when(checkoutMapper.toCheckoutDto(held, null)).thenReturn(new CheckoutDto(
            checkoutId,
            "HELD",
            facilityId,
            null,
            checkIn,
            checkOut,
            null,
            new BigDecimal("12.50"),
            "EUR",
            null,
            null));

        ResponseEntity<CheckoutDto> response = controller.selectOption(
            new SelectOptionRequest(facilityId, checkIn, checkOut),
            BOOKING_SESSION_ID,
            new MockHttpServletRequest(),
            authentication);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().checkoutId()).isEqualTo(checkoutId);
        assertThat(response.getBody().amount()).isEqualByComparingTo("12.50");
        assertThat(response.getBody().currency()).isEqualTo("EUR");
        assertThat(response.getBody().status()).isEqualTo("HELD");
        ArgumentCaptor<StartReservationRequest> requestCaptor = ArgumentCaptor.forClass(StartReservationRequest.class);
        verify(reservationsApi).startWithHttpInfo(requestCaptor.capture());
        assertThat(requestCaptor.getValue().getUserId()).isEqualTo(userId);
        assertThat(requestCaptor.getValue().getBookingSessionId()).isEqualTo(BOOKING_SESSION_ID);
        verify(reservationsApi).getWithHttpInfo(checkoutId);
    }

    @Test
    void findUserReservationsDelegatesUserFilterToFacilities() {
        String userId = "user-123";
        UUID userReservationId = UUID.randomUUID();
        UUID facilityId = UUID.randomUUID();
        LocalDateTime start = LocalDateTime.of(2026, 6, 7, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 6, 8, 0, 0);

        Reservation userReservation = reservation(userReservationId, facilityId, start.plusHours(10), start.plusHours(12))
            .userId(userId)
            .status("HELD");
        PageResponse pageResponse = new PageResponse().content(List.of(userReservation));
        Authentication authentication = mock(Authentication.class);

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(userId);
        when(reservationsApi.callListWithHttpInfo(
            eq(start),
            eq(end),
            any(org.labcabrera.parking.bff.generated.client.facilities.model.Pageable.class),
            eq(facilityId),
            eq(userId),
            isNull())).thenReturn(ResponseEntity.ok(pageResponse));
        when(ordersApi.findAllWithHttpInfo(
            any(org.labcabrera.parking.bff.generated.client.ecommerce.model.Pageable.class),
            isNull(),
            eq(userReservationId))).thenReturn(ResponseEntity.ok(new PageResponseOrder()));
        when(checkoutMapper.toCheckoutDto(userReservation, null)).thenReturn(new CheckoutDto(
            userReservationId,
            "HELD",
            facilityId,
            null,
            start.plusHours(10),
            start.plusHours(12),
            null,
            null,
            null,
            null,
            null));

        ResponseEntity<List<CheckoutDto>> response = controller.findUserReservations(
            start,
            end,
            facilityId,
            0,
            20,
            null,
            authentication);

        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).checkoutId()).isEqualTo(userReservationId);
    }

    private Reservation reservation(UUID reservationId, UUID facilityId, LocalDateTime checkIn, LocalDateTime checkOut) {
        return new Reservation()
            .id(reservationId)
            .facilityId(facilityId)
            .userId("user-test")
            .bookingSessionId(BOOKING_SESSION_ID)
            .checkIn(checkIn)
            .checkOut(checkOut);
    }
}
