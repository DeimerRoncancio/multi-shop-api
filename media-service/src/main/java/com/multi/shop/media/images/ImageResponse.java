package com.multi.shop.media.images;

public record ImageResponse(
    String id,
    String name,
    String imageUrl,
    String imageId
) {
    static ImageResponse of(Image image) {
        return new ImageResponse(image.getId(), image.getName(), image.getImageUrl(), image.getImageId());
    }
}
