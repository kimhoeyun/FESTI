package com.example.FESTI.domain.auth.repository;

import com.example.FESTI.domain.auth.entity.OAuthProvider;
import com.example.FESTI.domain.auth.entity.SocialAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {

    Optional<SocialAccount> findByProviderAndProviderUserId(OAuthProvider provider, String providerUserId);
}
