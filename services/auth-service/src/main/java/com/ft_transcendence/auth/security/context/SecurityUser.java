package com.ft_transcendence.auth.security.context;

import lombok.NonNull;
import com.ft_transcendence.auth.domain.model.UserAuth;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;

public record SecurityUser(UserAuth userAuth) implements UserDetails {

    @Override
    public @NonNull String getUsername() {
        // Essential: Spring Security uses this string token inside the principal identity context.
        // Mapping your raw database ID or business UUID ensures it remains immutable.
        return userAuth.getUserId().toString();
    }

    @Override
    public String getPassword() {
        return userAuth.getPassword();
    }

    @Override
    public @NonNull Collection<? extends GrantedAuthority> getAuthorities() {
        // Optimization: Swapped .collect(Collectors.toList()) to an unmodifiable List.
        // Context granted authorities should remain strictly immutable during a request runtime lifecycle.
        return userAuth.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .toList();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Set default safe fallback explicitly to bypass default interface rejection loops
    }

    @Override
    public boolean isAccountNonLocked() {
        return !userAuth.isAccountLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Set default safe fallback explicitly to bypass default interface rejection loops
    }

    @Override
    public boolean isEnabled() {
        return userAuth.isEnabled() && !userAuth.isDeleted();
    }

}