package com.example.FESTI.domain.user.repository;

import com.example.FESTI.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByCellphone(String cellphone);

    Optional<User> findByCellphone(String cellphone);
}
