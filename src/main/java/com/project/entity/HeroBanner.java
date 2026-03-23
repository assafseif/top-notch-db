package com.project.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "hero_banner", schema = "dbo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class HeroBanner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "hero_banner_id")
    private List<Image> images;

    private String subtitle;
    private String title;
    private String description;
    private String cta;
    private String link;
}
