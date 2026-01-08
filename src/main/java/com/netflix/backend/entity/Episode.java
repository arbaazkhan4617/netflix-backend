package com.netflix.backend.entity;

import jakarta.persistence.*;

import java.sql.Time;

@Entity
public class Episode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private Integer episodeNumber;

    private Integer duration;

    private String videoUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id", nullable = false)
    private Season season;
}
