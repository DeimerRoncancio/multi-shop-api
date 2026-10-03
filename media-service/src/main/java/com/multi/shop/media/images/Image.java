package com.multi.shop.media.images;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;

@Entity
@Table(name = "images")
public class Image {
    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    private String name;

    private String imageUrl;

    private String imageId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private ImageStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Image() {
    }

    public Image(String name, String imageUrl, String imageId, Instant createdAt) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.imageId = imageId;
        this.createdAt = createdAt;
        this.status = ImageStatus.PENDING;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getImageId() {
        return imageId;
    }

    public ImageStatus getStatus() {
        return status;
    }

    public void setStatus(ImageStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
