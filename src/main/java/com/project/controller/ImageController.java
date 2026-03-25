package com.project.controller;

import com.project.entity.Image;
import com.project.repository.ImageRepository;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    private final ImageRepository imageRepository;

    public ImageController(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id, ServletWebRequest request) throws IOException {
        Image image = imageRepository.findById(id).orElse(null);
        if (image == null || image.getData() == null || image.getData().length == 0) {
            return ResponseEntity.notFound().build();
        }

        return buildImageResponse(
                image.getData(),
                image.getContentType(),
                CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic(),
                request
        );
    }

    private ResponseEntity<byte[]> buildImageResponse(
            byte[] data,
            String contentType,
            CacheControl cacheControl,
            ServletWebRequest request
    ) throws IOException {
        String etag = '"' + DigestUtils.md5DigestAsHex(data) + '"';
        if (request.checkNotModified(etag)) {
            return ResponseEntity.status(304)
                    .eTag(etag)
                    .cacheControl(cacheControl)
                    .build();
        }

        return ResponseEntity.ok()
                .contentType(resolveMediaType(contentType, data))
                .contentLength(data.length)
                .cacheControl(cacheControl)
                .eTag(etag)
                .body(data);
    }

    private MediaType resolveMediaType(String explicitContentType, byte[] data) throws IOException {
        if (explicitContentType != null && !explicitContentType.isBlank()) {
            return MediaType.parseMediaType(explicitContentType);
        }

        String guessedContentType = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(data));
        if (guessedContentType != null && !guessedContentType.isBlank()) {
            return MediaType.parseMediaType(guessedContentType);
        }

        return MediaType.APPLICATION_OCTET_STREAM;
    }
}