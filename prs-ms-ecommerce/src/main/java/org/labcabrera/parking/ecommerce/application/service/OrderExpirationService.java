package org.labcabrera.parking.ecommerce.application.service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.labcabrera.parking.ecommerce.application.cqrs.command.ExpireOrderCommand;
import org.labcabrera.parking.ecommerce.application.port.OrderReadRepository;
import org.labcabrera.parking.ecommerce.domain.aggregate.Order;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderExpirationService {

    private final OrderReadRepository orderReadRepository;
    private final CommandGateway commandGateway;

    @Value("${ecommerce.order.expiration-scan-batch-size:100}")
    private int batchSize;

    @Scheduled(fixedDelayString = "${ecommerce.order.expiration-scan-fixed-delay-ms:30000}")
    public void expirePaymentWindows() {
        var expiredOrders = orderReadRepository.findExpiredPaymentWindow(LocalDateTime.now(), batchSize);
        if (expiredOrders.isEmpty()) {
            return;
        }

        log.info("Found {} ecommerce orders with expired payment window", expiredOrders.size());
        for (Order order : expiredOrders) {
            try {
                commandGateway.sendAndWait(new ExpireOrderCommand(order.getId()), 10, TimeUnit.SECONDS);
                log.info("Expired ecommerce order {} for reservation {}", order.getId(), order.getHoldId());
            }
            catch (RuntimeException ex) {
                log.warn("Could not expire ecommerce order {}: {}", order.getId(), ex.getMessage());
            }
        }
    }
}
