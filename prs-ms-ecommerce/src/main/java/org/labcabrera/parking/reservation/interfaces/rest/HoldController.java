package org.labcabrera.parking.reservation.interfaces.rest;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.labcabrera.parking.reservation.application.commands.CreateHoldCommand;
import org.labcabrera.parking.reservation.application.commands.ReleaseHoldCommand;
import org.labcabrera.parking.reservation.domain.model.HoldReadModel;
import org.labcabrera.parking.reservation.domain.model.HoldStatus;
import org.labcabrera.parking.reservation.domain.model.Money;
import org.labcabrera.parking.reservation.domain.port.outbound.HoldRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * REST adapter for parking spot hold operations.
 *
 * POST   /api/v1/reservations/holds          → 202 Accepted (dispatches CreateHoldCommand)
 * GET    /api/v1/reservations/holds/{holdId} → 200 or 404
 * DELETE /api/v1/reservations/holds/{holdId} → 204 or 409 Conflict for terminal states
 */
@Tag(name = "Holds", description = "Parking spot hold management")
@RestController
@RequestMapping("/api/v1/reservations/holds")
public class HoldController {

    private static final Logger log = LoggerFactory.getLogger(HoldController.class);
    private static final Set<HoldStatus> TERMINAL_STATES = EnumSet.of(
            HoldStatus.EXPIRED, HoldStatus.RELEASED, HoldStatus.FAILED, HoldStatus.CONVERTED);

    private final CommandGateway commandGateway;
    private final HoldRepository holdRepository;
    private final RateLimiterRegistry rateLimiterRegistry;

    @Value("${reservation.hold.ttl-seconds:600}")
    private int holdTtlSeconds;

    // Dynamic per-key rate limiters cached in memory
    private final ConcurrentMap<String, RateLimiter> ipLimiters = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, RateLimiter> sessionLimiters = new ConcurrentHashMap<>();

    public HoldController(CommandGateway commandGateway,
                          HoldRepository holdRepository,
                          RateLimiterRegistry rateLimiterRegistry) {
        this.commandGateway = commandGateway;
        this.holdRepository = holdRepository;
        this.rateLimiterRegistry = rateLimiterRegistry;
    }

    /**
     * Create a hold. Returns 202 Accepted with holdId immediately; pricing is async.
     */
    @Operation(summary = "Create a parking spot hold", description = "Dispatches CreateHoldCommand. Pricing is calculated asynchronously — poll GET /holds/{holdId} until status becomes ACTIVE.")
    @ApiResponses({
        @ApiResponse(responseCode = "202", description = "Hold accepted, pricing pending"),
        @ApiResponse(responseCode = "429", description = "Rate limit exceeded"),
        @ApiResponse(responseCode = "400", description = "Invalid request body")
    })
    @PostMapping
    public ResponseEntity<HoldCreatedResponse> createHold(
            @Valid @RequestBody CreateHoldRequest request,
            HttpServletRequest httpRequest) {

        String clientIp = resolveClientIp(httpRequest);
        checkRateLimit(ipLimiters, "hold-creation-by-ip", clientIp);
        checkRateLimit(sessionLimiters, "hold-creation-by-session", request.searchSessionId());

        UUID holdId = UUID.randomUUID();
        MDC.put("holdId", holdId.toString());
        MDC.put("searchSessionId", request.searchSessionId());
        try {
            Instant expiresAt = Instant.now().plusSeconds(holdTtlSeconds);
            Money estimatedPrice = new Money(request.estimatedPriceAmount(), request.currency());

            CreateHoldCommand cmd = new CreateHoldCommand(
                    holdId, request.searchSessionId(), request.spotId(), request.facilityId(),
                    clientIp, request.checkIn(), request.checkOut(), estimatedPrice, expiresAt);

            commandGateway.sendAndWait(cmd);
            log.info("Hold created [{}] for session [{}]", holdId, request.searchSessionId());

            return ResponseEntity.accepted().body(new HoldCreatedResponse(holdId, expiresAt));
        } finally {
            MDC.remove("holdId");
            MDC.remove("searchSessionId");
        }
    }

    /**
     * Get hold state by holdId.
     */
    @Operation(summary = "Get hold status")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Hold found"),
        @ApiResponse(responseCode = "404", description = "Hold not found or purged")
    })
    @GetMapping("/{holdId}")
    public ResponseEntity<HoldResponse> getHold(@Parameter(description = "Hold UUID") @PathVariable UUID holdId) {
        Optional<HoldReadModel> hold = holdRepository.findById(holdId);
        return hold
                .map(h -> ResponseEntity.ok(HoldResponse.from(h)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Release a hold. Returns 409 Conflict if the hold is in a terminal state.
     */
    @Operation(summary = "Release a hold")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Hold released"),
        @ApiResponse(responseCode = "404", description = "Hold not found"),
        @ApiResponse(responseCode = "409", description = "Hold is in terminal state")
    })
    @DeleteMapping("/{holdId}")
    public ResponseEntity<Void> releaseHold(@Parameter(description = "Hold UUID") @PathVariable UUID holdId) {
        Optional<HoldReadModel> holdOpt = holdRepository.findById(holdId);
        if (holdOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        HoldReadModel hold = holdOpt.get();
        if (TERMINAL_STATES.contains(hold.status())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        commandGateway.sendAndWait(new ReleaseHoldCommand(holdId));
        return ResponseEntity.noContent().build();
    }

    // --- helpers ---

    private void checkRateLimit(ConcurrentMap<String, RateLimiter> cache,
                                String registryName, String key) {
        RateLimiterConfig config = rateLimiterRegistry.rateLimiter(registryName).getRateLimiterConfig();
        RateLimiter limiter = cache.computeIfAbsent(key, k ->
                RateLimiter.of(registryName + ":" + k, config));
        if (!limiter.acquirePermission()) {
            throw RequestNotPermitted.createRequestNotPermitted(limiter);
        }
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Minimal response after hold creation.
     */
    public record HoldCreatedResponse(UUID holdId, Instant expiresAt) {}

    /**
     * Global handler for rate limit exceeded.
     */
    @ExceptionHandler(RequestNotPermitted.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public void handleRateLimit(RequestNotPermitted ex) {
        log.warn("Rate limit exceeded: {}", ex.getMessage());
    }
}
