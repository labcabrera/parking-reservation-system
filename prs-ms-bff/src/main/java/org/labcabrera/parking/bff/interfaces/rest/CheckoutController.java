package org.labcabrera.parking.bff.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import org.labcabrera.parking.bff.generated.client.ecommerce.api.OrdersApi;
import org.labcabrera.parking.bff.generated.client.ecommerce.model.InitiatePaymentRequest;
import org.labcabrera.parking.bff.generated.client.ecommerce.model.Order;
import org.labcabrera.parking.bff.generated.client.ecommerce.model.Pageable;
import org.labcabrera.parking.bff.generated.client.facilities.api.ParkingFacilitiesApi;
import org.labcabrera.parking.bff.generated.client.facilities.api.ReservationsApi;
import org.labcabrera.parking.bff.generated.client.facilities.model.FacilityAvailability;
import org.labcabrera.parking.bff.generated.client.facilities.model.PageResponse;
import org.labcabrera.parking.bff.generated.client.facilities.model.Reservation;
import org.labcabrera.parking.bff.generated.client.facilities.model.StartReservationRequest;
import org.labcabrera.parking.bff.interfaces.rest.dto.CheckoutDto;
import org.labcabrera.parking.bff.interfaces.rest.dto.InitiateCheckoutPaymentRequest;
import org.labcabrera.parking.bff.interfaces.rest.dto.ParkingOptionDto;
import org.labcabrera.parking.bff.interfaces.rest.dto.PaymentAttemptResultDto;
import org.labcabrera.parking.bff.interfaces.rest.dto.SelectOptionRequest;
import org.labcabrera.parking.bff.interfaces.rest.mapper.CheckoutMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.client.RestClientResponseException;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/checkout")
@Tag(name = "Checkout", description = "End-to-end parking reservation and payment flow")
@Slf4j
public class CheckoutController {

    private static final String BOOKING_SESSION_HEADER = "X-Booking-Session-Id";

    private final ParkingFacilitiesApi parkingFacilitiesApi;
    private final ReservationsApi reservationsApi;
    private final OrdersApi ordersApi;
    private final CheckoutMapper checkoutMapper;

    @Value("${bff.mock-payment.redirect-base-url:http://localhost:3001/payment}")
    private String mockPaymentRedirectBaseUrl;

    @Value("${bff.checkout.reservation-refresh-attempts:5}")
    private int reservationRefreshAttempts;

    @Value("${bff.checkout.reservation-refresh-delay-ms:100}")
    private long reservationRefreshDelayMs;

    public CheckoutController(
        ParkingFacilitiesApi parkingFacilitiesApi,
        ReservationsApi reservationsApi,
        OrdersApi ordersApi,
        CheckoutMapper checkoutMapper) {
        this.parkingFacilitiesApi = parkingFacilitiesApi;
        this.reservationsApi = reservationsApi;
        this.ordersApi = ordersApi;
        this.checkoutMapper = checkoutMapper;
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
            : response.getBody().stream().map(checkoutMapper::toParkingOptionDto).toList();

        return ResponseEntity.ok(options);
    }

    /**
     * Selects a parking option and creates a hold (reservation). The returned
     * {@code checkoutId} must be used in all subsequent operations. The hold expires
     * after a short window; the user must confirm before it lapses.
     */
    @PostMapping("/select-option")
    @Operation(summary = "Select a parking option", description = "Creates a time-limited hold on the chosen facility for the requested period. "
        + "Returns the checkout state including the expiry time.")
    public ResponseEntity<CheckoutDto> selectOption(
        @Valid @RequestBody SelectOptionRequest request,
        @RequestHeader(name = BOOKING_SESSION_HEADER, required = false) String bookingSessionHeader,
        HttpServletRequest httpRequest,
        Authentication authentication) {

        String bookingSessionId = resolveBookingSessionId(bookingSessionHeader, httpRequest);
        String userId = authenticatedUserIdOrNull(authentication);
        log.info("Received select-option for facilityId={}, checkIn={}, checkOut={}, userIdPresent={}, bookingSessionId={}",
            request.facilityId(), request.checkIn(), request.checkOut(), userId != null, bookingSessionId);
        StartReservationRequest startRequest = new StartReservationRequest()
            .facilityId(request.facilityId())
            .userId(userId)
            .bookingSessionId(bookingSessionId)
            .checkIn(request.checkIn())
            .checkOut(request.checkOut());
        Reservation reservation = reservationsApi.startWithHttpInfo(startRequest).getBody();
        return ResponseEntity.ok(checkoutMapper.toCheckoutDto(awaitPricedReservation(reservation), null));
    }

    @PostMapping("/{ignoredCheckoutId}/select-option")
    @Operation(summary = "Select a parking option", description = "Compatibility endpoint. The client-provided checkout id is ignored; "
        + "the returned checkout id is always the reservation id created by the reservation service.")
    public ResponseEntity<CheckoutDto> selectOptionWithClientId(
        @Parameter(description = "Deprecated client-provided checkout id, ignored by the BFF") @PathVariable UUID ignoredCheckoutId,
        @Valid @RequestBody SelectOptionRequest request,
        @RequestHeader(name = BOOKING_SESSION_HEADER, required = false) String bookingSessionHeader,
        HttpServletRequest httpRequest,
        Authentication authentication) {

        log.warn("Received deprecated select-option path with client-provided checkoutId={}; ignoring it", ignoredCheckoutId);
        return selectOption(request, bookingSessionHeader, httpRequest, authentication);
    }

    /**
     * Returns reservations owned by the authenticated user inside the requested time
     * window. The reservation id returned in each item is the checkout id to use in
     * follow-up checkout operations.
     */
    @GetMapping("/reservations")
    @Operation(summary = "Search current user's reservations", description = "Returns reservations owned by the authenticated user within the requested time window.")
    public ResponseEntity<List<CheckoutDto>> findUserReservations(
        @Parameter(description = "Window start (ISO-8601)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
        @Parameter(description = "Window end (ISO-8601)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
        @Parameter(description = "Optional facility filter") @RequestParam(required = false) UUID facilityId,
        @RequestParam(defaultValue = "0") Integer page,
        @RequestParam(defaultValue = "20") Integer size,
        @RequestParam(required = false) List<String> sort,
        Authentication authentication) {

        String userId = authenticatedUserId(authentication);
        var pageable = new org.labcabrera.parking.bff.generated.client.facilities.model.Pageable()
            .page(page)
            .size(size)
            .sort(sort);
        PageResponse response = reservationsApi.callListWithHttpInfo(start, end, pageable, facilityId, userId, null).getBody();
        List<CheckoutDto> reservations = response == null || response.getContent() == null
            ? List.of()
            : response.getContent().stream()
                .map(reservation -> checkoutMapper.toCheckoutDto(reservation, findOrderByHoldIdOrNull(reservation.getId())))
                .toList();
        return ResponseEntity.ok(reservations);
    }

    /**
     * Confirms the hold, locking in the reservation and triggering order creation. The
     * checkout must be in {@code PENDING} status.
     */
    @PostMapping("/{checkoutId}/confirm")
    @Operation(summary = "Confirm the checkout hold", description = "Confirms the parking reservation. An order is created automatically. "
        + "After confirmation the user can proceed to payment.")
    public ResponseEntity<CheckoutDto> confirm(
        @Parameter(description = "Checkout identifier returned by select-option") @PathVariable UUID checkoutId,
        @RequestHeader(name = BOOKING_SESSION_HEADER, required = false) String bookingSessionHeader,
        HttpServletRequest httpRequest,
        Authentication authentication) {

        log.info("Received confirm for checkoutId={}", checkoutId);
        String bookingSessionId = resolveBookingSessionId(bookingSessionHeader, httpRequest);
        Reservation existingReservation = findReservationByIdOrHeldForCaller(checkoutId, authentication, bookingSessionId);
        assertCheckoutBelongsToCaller(existingReservation, authentication, bookingSessionId);
        reservationsApi.confirmWithHttpInfo(existingReservation.getId());
        Reservation reservation = findReservationById(existingReservation.getId());
        Order order = awaitOrder(existingReservation.getId());
        return ResponseEntity.ok(checkoutMapper.toCheckoutDto(reservation, order));
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
        @Valid @RequestBody InitiateCheckoutPaymentRequest request,
        @RequestHeader(name = BOOKING_SESSION_HEADER, required = false) String bookingSessionHeader,
        HttpServletRequest httpRequest,
        Authentication authentication) {

        log.info("Received payment attempt for checkoutId={}, paymentMethodCode={}", checkoutId, request.paymentMethodCode());
        String bookingSessionId = resolveBookingSessionId(bookingSessionHeader, httpRequest);
        Reservation reservation = findReservationByIdOrHeldForCaller(checkoutId, authentication, bookingSessionId);
        assertCheckoutBelongsToCaller(reservation, authentication, bookingSessionId);
        Order order = awaitOrder(reservation.getId());

        UUID idempotencyKey = request.idempotencyKey() != null
            ? request.idempotencyKey()
            : UUID.randomUUID();
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
        @Parameter(description = "Checkout identifier returned by select-option") @PathVariable UUID checkoutId,
        @RequestHeader(name = BOOKING_SESSION_HEADER, required = false) String bookingSessionHeader,
        HttpServletRequest httpRequest,
        Authentication authentication) {

        String bookingSessionId = resolveBookingSessionId(bookingSessionHeader, httpRequest);
        Reservation reservation = findReservationByIdOrHeldForCaller(checkoutId, authentication, bookingSessionId);
        assertCheckoutBelongsToCaller(reservation, authentication, bookingSessionId);
        Order order = findOrderByHoldIdOrNull(reservation.getId());
        return ResponseEntity.ok(checkoutMapper.toCheckoutDto(reservation, order));
    }

    private Reservation findReservationById(UUID checkoutId) {
        return reservationsApi.getWithHttpInfo(checkoutId).getBody();
    }

    private Reservation findReservationByIdOrHeldForCaller(UUID checkoutId, Authentication authentication, String bookingSessionId) {
        try {
            return findReservationById(checkoutId);
        }
        catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() != HttpStatus.NOT_FOUND.value()) {
                throw exception;
            }
            return findHeldReservationForCaller(checkoutId, authentication, bookingSessionId);
        }
    }

    private Reservation findHeldReservationForCaller(UUID requestedCheckoutId, Authentication authentication, String bookingSessionId) {
        String userId = authenticatedUserIdOrNull(authentication);
        var pageable = new org.labcabrera.parking.bff.generated.client.facilities.model.Pageable()
            .page(0)
            .size(10);
        PageResponse response = reservationsApi.callListWithHttpInfo(
            LocalDateTime.now().minusDays(1),
            LocalDateTime.now().plusYears(5),
            pageable,
            null,
            userId,
            userId == null ? bookingSessionId : null).getBody();

        List<Reservation> heldReservations = response == null || response.getContent() == null
            ? List.of()
            : response.getContent().stream()
                .filter(reservation -> "HELD".equals(reservation.getStatus()))
                .toList();

        if (heldReservations.size() == 1) {
            Reservation resolvedReservation = heldReservations.get(0);
            log.warn("Resolved stale checkoutId={} to held reservation {} for current caller", requestedCheckoutId,
                resolvedReservation.getId());
            return resolvedReservation;
        }

        throw new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Checkout %s not found and current caller has %d held reservations".formatted(requestedCheckoutId, heldReservations.size()));
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

    private Order awaitOrder(UUID holdId) {
        for (int attempt = 0; attempt < reservationRefreshAttempts; attempt++) {
            if (attempt > 0) {
                sleepBeforeReservationRefresh();
            }
            Order order = findOrderByHoldIdOrNull(holdId);
            if (order != null) {
                return order;
            }
        }
        throw new IllegalStateException("No order found for checkout " + holdId + ". Confirm the checkout first.");
    }

    private Order findOrderByHoldIdOrNull(UUID holdId) {
        Pageable pageable = new Pageable().page(0).size(1);
        var response = ordersApi.findAllWithHttpInfo(pageable, null, holdId);
        if (response.getBody() == null || response.getBody().getContent().isEmpty()) {
            return null;
        }
        return response.getBody().getContent().get(0);
    }

    private String authenticatedUserId(Authentication authentication) {
        String userId = authenticatedUserIdOrNull(authentication);
        if (userId == null) {
            throw new IllegalStateException("Authenticated user is required");
        }
        return userId;
    }

    private String authenticatedUserIdOrNull(Authentication authentication) {
        if (authentication == null
            || !authentication.isAuthenticated()
            || authentication.getName() == null
            || authentication.getName().isBlank()
            || "anonymousUser".equals(authentication.getName())) {
            return null;
        }
        return authentication.getName();
    }

    private String resolveBookingSessionId(String bookingSessionHeader, HttpServletRequest httpRequest) {
        if (hasText(bookingSessionHeader)) {
            return bookingSessionHeader.trim();
        }
        return httpRequest.getSession(true).getId();
    }

    private void assertCheckoutBelongsToCaller(Reservation reservation, Authentication authentication, String bookingSessionId) {
        String userId = authenticatedUserIdOrNull(authentication);
        if (hasText(reservation.getUserId()) && reservation.getUserId().equals(userId)) {
            return;
        }
        if (!hasText(reservation.getUserId())
            && hasText(reservation.getBookingSessionId())
            && reservation.getBookingSessionId().equals(bookingSessionId)) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Checkout does not belong to the current caller");
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
