package com.example.FESTI.infrastructure.security;

import com.example.FESTI.domain.user.entity.Role;

public record UserPrincipal(Long userId, Role role) {
}
