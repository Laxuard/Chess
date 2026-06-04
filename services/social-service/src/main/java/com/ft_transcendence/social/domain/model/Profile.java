package com.ft_transcendence.social.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "profile")
@EntityListeners(AuditingEntityListener.class)
public class Profile {

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
