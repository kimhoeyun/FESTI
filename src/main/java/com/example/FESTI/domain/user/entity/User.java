package com.example.FESTI.domain.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(unique = true, length = 20)
    private String cellphone;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected User() {
    }

    public User(Role role, String name, String cellphone) {
        this.role = role == null ? Role.CUSTOMER : role;
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.cellphone = cellphone;
    }

    public void updateCellphone(String cellphone) {
        if (cellphone == null || cellphone.isBlank()) {
            throw new IllegalArgumentException("cellphone must not be blank");
        }
        this.cellphone = cellphone;
    }
}
