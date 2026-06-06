package org.labcabrera.parking.bff.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.labcabrera.parking.bff.generated.client.ecommerce.api.OrdersApi;
import org.labcabrera.parking.bff.generated.client.ecommerce.model.InitiatePaymentRequest;
import org.labcabrera.parking.bff.generated.client.ecommerce.model.Order;
import org.labcabrera.parking.bff.generated.client.ecommerce.model.Pageable;
import org.labcabrera.parking.bff.generated.client.facilities.api.ParkingFacilitiesApi;
import org.labcabrera.parking.bff.generated.client.facilities.api.ReservationsApi;
import org.labcabrera.parking.bff.generated.client.facilities.model.FacilityAvailability;
import org.labcabrera.parking.bff.generated.client.facilities.model.Reservation;
import org.labcabrera.parking.bff.generated.client.facilities.model.StartReservationRequest;
import org.labcabrera.parking.bff.interfaces.rest.dto.CheckoutDto;
import org.labcabrera.parking.bff.interfaces.rest.dto.InitiateCheckoutPaymentRequest;
import org.labcabrera.parking.bff.interfaces.rest.dto.ParkingOptionDto;
import org.labcabrera.parking.bff.interfaces.rest.dto.PaymentAttemptResultDto;
import org.labcabrera.parking.bff.interfaces.rest.dto.SelectOptionRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/checkout")
@Tag(name = "Checkout", description = "End-to-end parking reservation and payment flow")
public class CheckoutController {

    private final ParkingFacilitiesApi parkingFacilitiesApi;
    private final ReservationsApi reservationsApi;
    private final OrdersApi ordersApi;

    @Value("${bff.mock-payment.redirect-base-url:http://localhost:3001/payment}")
    private String mockPaymentRedirectBaseUrl;

    @Value("${bff.checkout.reservation-refresh-attempts:5}")
    private int reservationRefreshAttempts;

    @Value("${bff.checkout.reservation-refresh-delay-ms:100}")
    private long reservationRefreshDelayMs;

    public CheckoutController(
        ParkingFacilitiesApi parkingFacilitiesApi,
        ReservationsApi reservationsApi,
        OrdersApi ordersApi) {
        this.parkingFacilitiesApi = parkingFacilitiesApi;
        this.reservationsApi = reservationsApi;
        this.ordersApi = ordersApi;
    }

    /**
     * Searches for available parking facilities matching the given location and time
     * window. Returns a list of options that can be selected to start a checkout session.
     */
    @GetMapping("/search")
    @Operation(summary = "Search available parking options", description = "Returns parking facilities available for the requested location, check-in and check-out times.")
    public ResponseEntity<List<ParkingOptionDto>> search(
        @Parameter(description = "Location query (city, address or keyword)") @RequestParam String q,
        @Parameter(description = "Desired check-in time (ISO-8601)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkIn,
        @Parameter(description = "Desired check-out time (ISO-8601)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkOut,
        @Parameter(description = "Maximum number of results") @RequestParam(required = false) Integer limit) {

        ResponseEntity<List<FacilityAvailability>> response = parkingFacilitiesApi.searchAvailabilityWithHttpInfo(q, checkIn, checkOut,
            limit);

        List<ParkingOptionDto> options = response.getBody() == null
            ? List.of()
            : response.getBody().stream().map(this::toParkingOptionDto).toList();

        return ResponseEntity.ok(options);
    }

    /**
     * Selects a parking option and creates a hold (reservation). The returned
     * {@code checkoutId} must be used in all subsequent operations. The hold expires
     * after a short window; the user must confirm before it lapses.
     */
    @PostMapping("/{checkoutId}/select-option")
    @Operation(summary = "Select a parking option", description = "Creates a time-limited hold on the chosen facility for the requested period. "
        + "Returns the checkout state including the expiry time.")
    public ResponseEntity<CheckoutDto> selectOption(
        @Parameter(description = "Client-generated idempotency key for this checkout session") @PathVariable UUID checkoutId,
        @Valid @RequestBody SelectOptionRequest request) {

        StartReservationRequest startRequest = new StartReservationRequest()
            .facilityId(request.facilityId())
            .checkIn(request.checkIn())
            .checkOut(request.checkOut());

        Reservation reservation = reservationsApi.startWithHttpInfo(startRequest).getBody();
        return ResponseEntity.ok(toCheckoutDto(awaitPricedReservation(reservation), null));
    }

    /**
     * Confirms the hold, locking in the reservation and triggering order creation. The
     * checkout must be in {@code PENDING} status.
     */
    @PostMapping("/{checkoutId}/confirm")
    @Operation(summary = "Confirm the checkout hold", description = "Confirms the parking reservation. An order is created automatically. "
        + "After confirmation the user can proceed to payment.")
    public ResponseEntity<CheckoutDto> confirm(
        @Parameter(description = "Checkout identifier returned by select-option") @PathVariable UUID checkoutId) {

        reservationsApi.confirmWithHttpInfo(checkoutId);
        Reservation reservation = reservationsApi.getWithHttpInfo(checkoutId).getBody();
        return ResponseEntity.ok(toCheckoutDto(reservation, null));
    }

    /**
     * Initiates a payment attempt for a confirmed checkout. Returns a {@code redirectUrl}
     * where the user must be sent to complete the payment.
     */
    @PostMapping("/{checkoutId}/payment-attempts")
    @Operation(summary = "Initiate a payment attempt", description = "Initiates the payment flow for the confirmed order. "
        + "The response contains the redirect URL to the payment gateway page.")
    public ResponseEntity<PaymentAttemptResultDto> pay(
        @Parameter(description = "Checkout identifier returned by select-option") @PathVariable UUID checkoutId,
        @Valid @RequestBody InitiateCheckoutPaymentRequest request) {

        Order order = findOrderByHoldId(checkoutId);

        UUID idempotencyKey = UUID.randomUUID();
        ordersApi.payWithHttpInfo(order.getId(), new InitiatePaymentRequest()
            .idempotencyKey(idempotencyKey)
            .paymentMethodCode(request.paymentMethodCode()));

        String redirectUrl = mockPaymentRedirectBaseUrl
            + "?orderId=" + order.getId()
            + "&amount=" + order.getAmount()
            + "&currency=" + order.getCurrency()
            + "&attemptId=" + idempotencyKey;

        return ResponseEntity.ok(new PaymentAttemptResultDto(idempotencyKey, "PENDING", redirectUrl));
    }

    /**
     * Returns the current state of the checkout, including reservation details and
     * payment status when available.
     */
    @GetMapping("/{checkoutId}")
    @Operation(summary = "Get checkout state", description = "Returns the full checkout state: reservation details, expiry time, amount and payment status.")
    public ResponseEntity<CheckoutDto> getCheckout(
        @Parameter(description = "Checkout identifier returned by select-option") @PathVariable UUID checkoutId) {

        Reservation reservation = reservationsApi.getWithHttpInfo(checkoutId).getBody();
        Order order = findOrderByHoldIdOrNull(checkoutId);
        return ResponseEntity.ok(toCheckoutDto(reservation, order));
    }

    // --- Mapping helpers ---

    private ParkingOptionDto toParkingOptionDto(FacilityAvailability fa) {
        return new ParkingOptionDto(
            fa.getId(),
            fa.getName(),
            fa.getCity(),
            fa.getAddress(),
            fa.getAvailableSpots(),
            Boolean.TRUE.equals(fa.getLowAvailability()),
            fa.getEstimatedPrice(),
            fa.getCurrency());
    }

    private CheckoutDto toCheckoutDto(Reservation reservation, Order order) {
        if (reservation == null) {
            return null;
        }
        String paymentStatus = order != null && order.getStatus() != null
            ? order.getStatus().getValue()
            : null;
        return new CheckoutDto(
            reservation.getId(),
            reservation.getStatus(),
            reservation.getFacilityId(),
            null,
            reservation.getCheckIn(),
            reservation.getCheckOut(),
            reservation.getExpiresAt(),
            reservation.getEstimatedPrice(),
            reservation.getCurrency(),
            paymentStatus,
            null);
    }

    private Reservation awaitPricedReservation(Reservation initialReservation) {
        if (initialReservation == null || initialReservation.getId() == null || hasPrice(initialReservation)) {
            return initialReservation;
        }

        Reservation current = initialReservation;
        for (int attempt = 0; attempt < reservationRefreshAttempts; attempt++) {
            if (attempt > 0) {
                sleepBeforeReservationRefresh();
            }
            Reservation refreshed = reservationsApi.getWithHttpInfo(initialReservation.getId()).getBody();
            if (refreshed != null) {
                current = refreshed;
                if (hasPrice(refreshed)) {
                    return refreshed;
                }
            }
        }
        return current;
    }

    private boolean hasPrice(Reservation reservation) {
        BigDecimal estimatedPrice = reservation.getEstimatedPrice();
        return estimatedPrice != null && reservation.getCurrency() != null && !reservation.getCurrency().isBlank();
    }

    private void sleepBeforeReservationRefresh() {
        try {
            Thread.sleep(reservationRefreshDelayMs);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private Order findOrderByHoldId(UUID holdId) {
        Order order = findOrderByHoldIdOrNull(holdId);
        if (order == null) {
            throw new IllegalStateException("No order found for checkout " + holdId + ". Confirm the checkout first.");
        }
        return order;
    }

    private Order findOrderByHoldIdOrNull(UUID holdId) {
        Pageable pageable = new Pageable().page(0).size(1);
        var response = ordersApi.findAllWithHttpInfo(pageable, null, holdId);
        if (response.getBody() == null || response.getBody().getContent().isEmpty()) {
            return null;
        }
        return response.getBody().getContent().get(0);
    }
}
