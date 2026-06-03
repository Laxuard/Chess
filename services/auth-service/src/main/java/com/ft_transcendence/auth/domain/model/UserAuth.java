package com.ft_transcendence.auth.domain.model;

import com.ft_transcendence.auth.domain.model.twofactor.UserTwoFactorMethod;
import lombok.*;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_auth_id"))
    private Set<UserRole> roles = new HashSet<>(Set.of(UserRole.USER));

    // === Account Lifecycle ===
    @Builder.Default
    private boolean enabled = true;

    @Builder.Default
    @Column(name = "account_locked")
    private boolean accountLocked = false;

    @Builder.Default
    private boolean deleted = false;

    // === Security Hardening ===
    @Builder.Default
    @Column(name = "auth_version")
    private Integer authVersion = 0;

    // === 2FA ===
    @Builder.Default
    @Column(name = "is_2fa_enabled")
    private boolean is2faEnabled = false;

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserTwoFactorMethod> twoFactorMethods = new ArrayList<>();

    public void addTwoFactorMethod(UserTwoFactorMethod method) {
        twoFactorMethods.add(method);
        method.setUser(this);
    }

    public void removeTwoFactorMethod(UserTwoFactorMethod method) {
        twoFactorMethods.remove(method);
        method.setUser(null);
    }

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
