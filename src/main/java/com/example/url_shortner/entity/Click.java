package com.example.url_shortner.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "clicks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Click {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "link_id", nullable = false)
    private Link link;

    @Column(name = "clicked_at", nullable = false, updatable = false)
    private LocalDateTime clickedAt;
}
