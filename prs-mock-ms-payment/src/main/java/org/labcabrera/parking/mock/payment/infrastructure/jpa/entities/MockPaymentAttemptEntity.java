package org.labcabrera.parking.mock.payment.infrastructure.jpa.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "mock_payment_attempts")
@Getter
@Setter
@NoArgsConstructor
public class MockPaymentAttemptEntity {

    @Id
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "idempotency_key", length = 100)
    private String idempotencyKey;

    @Column(name = "payment_method_code", length = 50)
    private String paymentMethodCode;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "redirect_url", nullable = false, length = 1000)
    private String redirectUrl;

    @Column(name = "customer_callback_url", nullable = false, length = 1000)
    private String customerCallbackUrl;

    @Column(name = "ecommerce_callback_url", length = 1000)
    private String ecommerceCallbackUrl;

    @Column(name = "gateway_transaction_id", length = 200)
    private String gatewayTransactionId;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Version
    private Long version;
}
