package com.example.FESTI.application.auth.dto;

import com.example.FESTI.domain.user.entity.Role;

public record MyProfile(
        Long id,
        String name,
        Role role,
        String cellphone,
        boolean profileCompleted
) {
}
