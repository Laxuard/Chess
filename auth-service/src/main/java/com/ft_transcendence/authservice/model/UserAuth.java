package com.ft_transcendence.authservice.model;

import lombok.*;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_auth")
@EntityListeners(AuditingEntityListener.class)
public class UserAuth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @Column(unique = true, nullable = false, updatable = false, name = "user_id")
    private UUID userId = UUID.randomUUID();

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true, nullable = false)
    private String username;

    private String password;

    @Builder.Default
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.USER;

    // === Account Lifecycle ===
    @Builder.Default
    private boolean enabled = true;

    @Builder.Default
    @Column(name = "account_locked")
    private boolean accountLocked = false;

    @Builder.Default
    private boolean deleted = false;

    // === Security Hardening ===

    // === 2FA ===
    @Builder.Default
    @Column(name = "is_2fa_enabled")
    private boolean is2faEnabled = false;

    @Column(name = "two_factor_secret")
    private String twoFactorSecret;

    // === OAuth ===
    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserIdentity> identities = new ArrayList<>();

    public void addIdentity(UserIdentity identity) {
        identities.add(identity);
        identity.setUser(this);
    }

    // === Auditing ===
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Integer version;

}
