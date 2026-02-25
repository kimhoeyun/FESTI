package com.example.FESTI.domain.payments.entity;

import com.example.FESTI.domain.order.entity.Order;
import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "payments",
        indexes = {
                @Index(name = "idx_payments_pg_tx_id", columnList = "pg_transaction_id"),
                @Index(name = "idx_payments_idempotency_key", columnList = "idempotency_key")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_payments_order_id", columnNames = "order_id"),
                @UniqueConstraint(name = "uk_payments_idempotency_key", columnNames = "idempotency_key")
        })
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    // 주문당 1결제 가정
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @Column(nullable = false)
    private int amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "pg_provider", nullable = false, length = 30)
    private PgProvider pgProvider;

    @Column(name = "pg_transaction_id", length = 100)
    private String pgTransactionId;

    @Column(name = "idempotency_key", nullable = false, length = 100)
    private String idempotencyKey;

    protected Payment() {}

    public Payment(Order order, int amount, PgProvider pgProvider, String idempotencyKey) {
        if (amount <= 0) throw new IllegalArgumentException("amount must be positive");
        this.order = order;
        this.amount = amount;
        this.pgProvider = pgProvider;
        this.idempotencyKey = idempotencyKey;
        this.status = Status.REQUESTED;
    }

    public void confirm(String pgTransactionId) {
        requireStatus(Status.REQUESTED);
        this.pgTransactionId = pgTransactionId;
        this.status = Status.CONFIRMED;
    }

    public void cancel() {
        if (this.status == Status.CANCELED) return;
        if (this.status == Status.CONFIRMED) {
            // 승인된 결제 취소를 허용할지(취소 API 연동) 결정 필요
            // 지금은 허용한다고 가정
        }
        this.status = Status.CANCELED;
    }

    private void requireStatus(Status expected) {
        if (this.status != expected) {
            throw new IllegalStateException("결제 상태가 올바르지 않습니다. expected=" + expected + ", actual=" + this.status);
        }
    }
}
