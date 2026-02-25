package com.example.FESTI.domain.order.entity;

import com.example.FESTI.domain.menu.entity.Menu;
import jakarta.persistence.*;
import lombok.Getter;

@Getter
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @Column(nullable = false)
    private int quantity;

    protected OrderItem() {}

    public OrderItem(Menu menu, int quantity) {
        if (menu == null) throw new IllegalArgumentException("menu must not be null");
        if (quantity <= 0) throw new IllegalArgumentException("quantity must be positive");

        this.menu = menu;
        this.quantity = quantity;
    }

    void setOrder(Order order) {
        this.order = order;
    }

    public void changeQuantity(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("quantity must be positive");
        this.quantity = quantity;
    }
}
