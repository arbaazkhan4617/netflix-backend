package com.netflix.backend.entity;

import jakarta.persistence.*;

import java.util.Set;

@Entity
public class Genre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;


//    @ManyToMany
//    @JoinTable(
//            name = "movie_genres",
//            joinColumns = @JoinColumn(name = "movie_id"),
//            inverseJoinColumns = @JoinColumn(name = "genre_id")
//    )
//    private Set<Genre> genres;

}
