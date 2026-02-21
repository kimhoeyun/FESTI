package com.example.FESTI.domain.order.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "order_items",
        indexes = {
                @Index(name = "idx_order_items_order_id", columnList = "order_id"),
                @Index(name = "idx_order_items_menu_id", columnList = "menu_id")
        })
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "menu_id", nullable = false)
    private Long menuId;

    @Column(nullable = false)
    private int quantity;

    protected OrderItem() {}

    public OrderItem(Long menuId, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("quantity must be positive");
        this.menuId = menuId;
        this.quantity = quantity;
    }

    // 연관관계 설정(외부에서 직접 호출 최소화)
    void setOrder(Order order) {
        this.order = order;
    }

    public void changeQuantity(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("quantity must be positive");
        this.quantity = quantity;
    }
}
