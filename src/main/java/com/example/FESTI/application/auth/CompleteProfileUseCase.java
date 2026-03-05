package com.example.FESTI.application.auth;

import com.example.FESTI.domain.user.entity.User;
import com.example.FESTI.domain.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompleteProfileUseCase {

    private final UserRepository userRepository;

    public CompleteProfileUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void updateCellphone(Long userId, String cellphone) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException("user not found"));

        userRepository.findByCellphone(cellphone)
                .filter(found -> !found.getId().equals(userId))
                .ifPresent(found -> {
                    throw new AuthException(HttpStatus.CONFLICT, "cellphone already exists");
                });

        user.updateCellphone(cellphone);
    }
}
