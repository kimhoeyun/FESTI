package com.example.FESTI.application.auth;

import com.example.FESTI.application.auth.dto.MyProfile;
import com.example.FESTI.domain.user.entity.User;
import com.example.FESTI.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MyProfileQueryService {

    private final UserRepository userRepository;

    public MyProfileQueryService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public MyProfile getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException("user not found"));

        return new MyProfile(
                user.getId(),
                user.getName(),
                user.getRole(),
                user.getCellphone(),
                user.getCellphone() != null && !user.getCellphone().isBlank()
        );
    }
}
