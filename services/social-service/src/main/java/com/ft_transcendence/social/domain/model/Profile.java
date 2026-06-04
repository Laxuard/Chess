package com.ft_transcendence.social.domain.model;

import com.ft_transcendence.common.model.BaseAuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "profile")
public class Profile extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // LOCAL PRIMARY KEY: Super fast for internal joins inside social-db

    @Column(unique = true, nullable = false, name = "user_id", updatable = false)
    private UUID userId;

    @Column(nullable = false)
    private String username; // Cached display name synced via Kafka

    @Column(name = "avatar_url", nullable = false)
    private String avatarUrl; // Cached avatar synced via Kafka

    // === Social Domain Specific Fields ===
    @Builder.Default
    @Column(columnDefinition = "TEXT")
    private String bio = "Hello, I am using Transcendence!";

    @Builder.Default
    @Column(name = "profile_hidden")
    private boolean profileHidden = false;

}
