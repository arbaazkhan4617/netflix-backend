package com.netflix.backend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class WatchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "profile_id", nullable = false)
//    private Profile profile;

    private Long contentId;

    @Enumerated(EnumType.STRING)
    private ContentType contentType;

    private Integer lastWatchedPosition; // seconds

    private LocalDateTime lastWatchedAt;
}
