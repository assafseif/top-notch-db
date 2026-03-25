package com.project.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "image", schema = "dbo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // optional filename or external URL
    @Column(name = "filename")
    private String filename;

    @Column(name = "content_type")
    private String contentType;

    // store image binary if you prefer DB storage (nullable if using external storage)
    @Lob
    @Column(name = "data", columnDefinition = "VARBINARY(MAX)")
    @JsonIgnore
    private byte[] data;

    public Image(String filename, String contentType) {
        this.filename = filename;
        this.contentType = contentType;
    }
}

