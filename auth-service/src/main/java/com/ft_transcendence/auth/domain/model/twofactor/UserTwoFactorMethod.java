package com.ft_transcendence.auth.domain.model.twofactor;

import com.ft_transcendence.auth.domain.model.UserAuth;
import lombok.*;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_two_factor_methods")
@EntityListeners(AuditingEntityListener.class)
public class UserTwoFactorMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAuth user;

    @Enumerated(EnumType.STRING)
    @Column(name = "method_type", nullable = false)
    private TwoFactorMethodType methodType;

    @Column(name = "secret_key", nullable = false)
    private String secretKey; // Holds the encrypted secret seed configuration payload

    @Builder.Default
    @Column(name = "is_verified", nullable = false)
    private boolean isVerified = false; // Set to true once the user successfully satisfies the registration challenge

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt; // Track usage statistics for audit trails

    // --- Auditing ---
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}