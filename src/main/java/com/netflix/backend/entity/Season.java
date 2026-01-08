package com.netflix.backend.entity;

import jakarta.persistence.*;

@Entity
public class Season {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer seasonNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", nullable = false)
    private Series series;

}
