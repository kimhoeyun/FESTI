package com.example.FESTI.domain.auth.entity;

import com.example.FESTI.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Entity
@Table(name = "social_accounts",
        indexes = {
                @Index(name = "idx_social_accounts_user_id", columnList = "user_id"),
                @Index(name = "idx_social_accounts_email", columnList = "email")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_social_accounts_provider_user", columnNames = {"provider", "provider_user_id"})
        })
@EntityListeners(AuditingEntityListener.class)
public class SocialAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OAuthProvider provider;

    @Column(name = "provider_user_id", nullable = false, length = 100)
    private String providerUserId;

    @Column(length = 255)
    private String email;

    @Column(nullable = false)
    private LocalDateTime linkedAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected SocialAccount() {
    }

    public SocialAccount(User user, OAuthProvider provider, String providerUserId, String email) {
        this.user = Objects.requireNonNull(user, "user must not be null");
        this.provider = Objects.requireNonNull(provider, "provider must not be null");
        this.providerUserId = Objects.requireNonNull(providerUserId, "providerUserId must not be null");
        this.email = email;
        this.linkedAt = LocalDateTime.now();
    }
}
