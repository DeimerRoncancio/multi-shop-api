package com.multi.shop.identity.users.mappers;

import com.multi.shop.identity.media.MediaApi;
import com.multi.shop.identity.media.StoredImage;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

@Component
public class ProfileImageMapper {
    private final MediaApi mediaApi;

    public ProfileImageMapper(MediaApi mediaApi) {
        this.mediaApi = mediaApi;
    }

    @Named("profileImage")
    public StoredImage profileImage(String profileImageId) {
        return mediaApi.findOne(profileImageId).orElse(null);
    }
}
