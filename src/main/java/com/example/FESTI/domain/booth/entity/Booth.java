package com.example.FESTI.domain.booth.entity;

import com.example.FESTI.domain.menu.entity.Menu;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "booths")
public class Booth {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false, length = 255)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @OneToMany(mappedBy = "booth", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Menu> menus = new ArrayList<>();

    protected Booth() {
    }

    public Booth(String name, String description, String location, Status status) {
        this.name = name;
        this.description = description;
        this.location = location;
        this.status = status;
    }

    public void open() {
        this.status = Status.OPEN;
    }

    public void close() {
        this.status = Status.CLOSED;
    }

    public void changeInfo(String name, String description, String location) {
        this.name = name;
        this.description = description;
        this.location = location;
    }
}
