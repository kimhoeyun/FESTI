package com.example.FESTI.domain.order.entity;

import jakarta.persistence.*;

import java.util.ArrayList;

@Entity
@Table(name = "orders",
        indexes = {
                @Index(name = "idx_orders_user_id", columnList = "user_id"),
                @Index(name = "idx_orders_booth_id", columnList = "booth_id")
        })
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "booth_id", nullable = false)
    private Long boothId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    protected Order() {}

    public Order(Long userId, Long boothId) {
        this.userId = userId;
        this.boothId = boothId;
        this.status = Status.PAID; // 결제 성공 후 주문 생성이라면 기본값을 PAID로
    }

    // ===== 연관관계 편의 메서드 =====
    public void addItem(OrderItem item) {
        this.items.add(item);
        item.setOrder(this);
    }

    public void removeItem(OrderItem item) {
        this.items.remove(item);
        item.setOrder(null);
    }

    // ===== 상태 전이 =====
    public void accept() {
        requireStatus(Status.PAID);
        this.status = Status.ACCEPTED;
    }

    public void startCooking() {
        requireStatus(Status.ACCEPTED);
        this.status = Status.COOKING;
    }

    public void ready() {
        requireStatus(Status.COOKING);
        this.status = Status.READY;
    }

    public void pickUp() {
        requireStatus(Status.READY);
        this.status = Status.PICKED_UP;
    }

    public void cancel() {
        if (this.status == Status.PICKED_UP) {
            throw new IllegalStateException("이미 픽업된 주문은 취소할 수 없습니다.");
        }
        this.status = Status.CANCELED;
    }

    private void requireStatus(Status expected) {
        if (this.status != expected) {
            throw new IllegalStateException("주문 상태가 올바르지 않습니다. expected=" + expected + ", actual=" + this.status);
        }
    }

    // (필요 시 Getter 추가 or Lombok @Getter 사용)
}
