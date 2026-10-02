package com.multi.shop.api.multi_shop_api.media.api;

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
