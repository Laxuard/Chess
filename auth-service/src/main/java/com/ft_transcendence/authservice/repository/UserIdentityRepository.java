package com.ft_transcendence.authservice.repository;

import java.util.Optional;
import com.ft_transcendence.authservice.model.AuthProvider;
import com.ft_transcendence.authservice.model.UserIdentity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserIdentityRepository extends JpaRepository<UserIdentity, Long> {

    Optional<UserIdentity> findByProviderAndProviderId(AuthProvider provider, String providerId);
    
}