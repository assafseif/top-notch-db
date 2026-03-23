package com.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "limited_banner", schema = "dbo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LimitedBanner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @OneToOne
    @JoinColumn(name = "image_id")
    private Image image;

    private String badge;
    private String title;
    private String description;
    private String cta;
    private String link;
}
