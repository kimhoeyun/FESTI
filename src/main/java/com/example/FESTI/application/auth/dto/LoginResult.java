package com.example.FESTI.application.auth.dto;

import com.example.FESTI.domain.user.entity.User;

public record LoginResult(
        User user,
        TokenPair tokenPair
) {
}
