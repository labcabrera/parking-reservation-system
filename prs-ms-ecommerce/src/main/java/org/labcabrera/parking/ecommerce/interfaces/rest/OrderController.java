package org.labcabrera.parking.ecommerce.interfaces.rest;

import java.net.URI;
import java.util.UUID;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.labcabrera.parking.ecommerce.application.cqrs.command.CreateOrderCommand;
import org.labcabrera.parking.ecommerce.application.cqrs.query.FindOrdersQuery;
import org.labcabrera.parking.ecommerce.application.cqrs.query.GetOrderByIdQuery;
import org.labcabrera.parking.ecommerce.application.service.OrderPaymentService;
import org.labcabrera.parking.ecommerce.domain.aggregate.Order;
import org.labcabrera.parking.ecommerce.domain.valueobject.OrderStatus;
import org.labcabrera.parking.ecommerce.interfaces.rest.dto.CreateOrderRequest;
import org.labcabrera.parking.ecommerce.interfaces.rest.dto.InitiatePaymentRequest;
import org.labcabrera.parking.ecommerce.interfaces.rest.dto.OrderDto;
import org.labcabrera.parking.ecommerce.interfaces.rest.dto.PageResponse;
import org.labcabrera.parking.ecommerce.interfaces.rest.dto.Pagination;
import org.labcabrera.parking.ecommerce.interfaces.rest.mapper.OrderMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "Create and manage ecommerce orders")
@RequiredArgsConstructor
@Validated
@Slf4j
public class OrderController {

    private final CommandGateway commandGateway;
    private final QueryGateway queryGateway;
    private final OrderMapper mapper;
    private final OrderPaymentService orderPaymentService;

    @PostMapping
    @Operation(summary = "Create an order from a hold")
    public ResponseEntity<OrderDto> create(@Valid @RequestBody CreateOrderRequest request) {
        UUID orderId = UUID.randomUUID();
        log.info("Received create order request {} for hold {}", orderId, request.holdId());
        var command = new CreateOrderCommand(
            orderId,
            request.holdId(),
            request.bookingSessionId(),
            request.expiresAt(),
            request.amount(),
            request.currency());
        commandGateway.sendAndWait(command);
        Order order = queryGateway
            .query(new GetOrderByIdQuery(orderId), ResponseTypes.instanceOf(Order.class))
            .join();
        return ResponseEntity
            .created(URI.create("/api/v1/orders/%s".formatted(orderId)))
            .body(mapper.toDto(order));
    }

    @GetMapping
    @Operation(summary = "Search orders with optional filters")
    @SuppressWarnings("unchecked")
    public ResponseEntity<PageResponse<OrderDto>> findAll(
        @RequestParam(required = false) OrderStatus status,
        @RequestParam(required = false) UUID holdId,
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        log.debug("Received find orders request — status={}, holdId={}", status, holdId);
        Page<Order> page = queryGateway
            .query(new FindOrdersQuery(status, holdId, pageable), ResponseTypes.instanceOf(Page.class))
            .join();
        Page<OrderDto> dtoPage = page.map(mapper::toDto);
        var response = new PageResponse<>(
            dtoPage.getContent(),
            new Pagination(dtoPage.getNumber(), dtoPage.getSize(), dtoPage.getTotalElements(), dtoPage.getTotalPages()));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order details")
    public ResponseEntity<OrderDto> getById(@PathVariable UUID id) {
        Order order = queryGateway
            .query(new GetOrderByIdQuery(id), ResponseTypes.instanceOf(Order.class))
            .join();
        return ResponseEntity.ok(mapper.toDto(order));
    }

    @PostMapping("/{id}/pay")
    @Operation(summary = "Initiate payment for an order", description = "Idempotent: re-submitting with the same idempotencyKey returns the "
        + "original result without charging the customer again.")
    public ResponseEntity<Void> pay(
        @PathVariable UUID id,
        @Valid @RequestBody InitiatePaymentRequest request) {
        log.info("Initiating payment for order {} (idempotencyKey={})", id, request.idempotencyKey());
        orderPaymentService.initiatePayment(id, request.idempotencyKey().toString(), request.paymentMethodCode());
        return ResponseEntity.ok().build();
    }
}
