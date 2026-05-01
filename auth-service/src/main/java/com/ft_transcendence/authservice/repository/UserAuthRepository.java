package com.ft_transcendence.authservice.repository;

import java.util.UUID;
import java.util.Optional;

import com.ft_transcendence.authservice.model.UserAuth;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAuthRepository extends JpaRepository<UserAuth, Long> {

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    Optional<UserAuth> findByEmail(String email);

    Optional<UserAuth> findByUserId(UUID userId);

    Optional<UserAuth> findByUsername(String username);


}