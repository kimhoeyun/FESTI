package com.example.FESTI.domain.menu.entity;

import com.example.FESTI.domain.booth.entity.Booth;
import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "menus", indexes = {@Index(name = "idx_menus_booth_id", columnList = "booth_id")})
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booth_id", nullable = false)
    private Booth booth;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false)
    private int price;

    @Column(length = 1024)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @Column(nullable = false)
    private int stock;


    protected Menu() {
    }

    public Menu(Booth booth, String name, String description, int price, String imageUrl, Status status, int stock) {
        this.booth = booth;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.status = status;
        this.stock = stock;
    }

    public void changeInfo(String name, String description, int price, String imageUrl) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
    }

    public void startSelling() {
        this.status = Status.SELLING;
    }

    public void soldOut() {
        this.status = Status.SOLD_OUT;
    }

    public void decreaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        if (this.stock < quantity) {
            throw new IllegalStateException("not enough stock");
        }
        this.stock -= quantity;
        if (this.stock == 0) {
            this.status = Status.SOLD_OUT;
        }
    }

    public void increaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        this.stock += quantity;
        if (this.stock > 0) {
            this.status = Status.SELLING;
        }
    }
}
