package com.multi.shop.catalog.media;

public record StoredImage(
    String id,
    String name,
    String imageUrl,
    String imageId
) {
    public StoredImage withName(String newName) {
        return new StoredImage(id, newName, imageUrl, imageId);
    }
}
