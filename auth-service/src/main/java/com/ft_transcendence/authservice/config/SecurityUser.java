package com.ft_transcendence.authservice.config;

import lombok.NonNull;
import com.ft_transcendence.authservice.model.UserAuth;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Collection;
import java.util.stream.Collectors;

public record SecurityUser(UserAuth userAuth) implements UserDetails {

    @Override
    public @NonNull String getUsername() {
        return userAuth.getUserId().toString();
    }


    @Override
    public String getPassword() {
        return userAuth.getPassword();
    }

    @Override
    public @NonNull Collection<? extends GrantedAuthority> getAuthorities() {
        return userAuth.getRoles()
                .stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.name()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isAccountNonLocked() {
        return !userAuth.isAccountLocked();
    }

    @Override
    public boolean isEnabled() {
        return userAuth.isEnabled() && !userAuth.isDeleted();
    }
}
