package com.multi.shop.api.multi_shop_api.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Service
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryServiceImpl() {
        Map<String, String> valuesMap = new HashMap<> ();
        valuesMap.put("cloud_name", "ddyibxicu");
        valuesMap.put("api_key", "136526763544122");
        valuesMap.put("api_secret", "bDgoglF6cLqEvwbSCsdJO47Ww1I");
        cloudinary = new Cloudinary(valuesMap);
    }
    
    @Override
    @SuppressWarnings("rawtypes")
    public Map upload(MultipartFile multipartFile) throws IOException {
        File file = convert(multipartFile);
        Map result = cloudinary.uploader().upload(file, ObjectUtils.emptyMap());

        if (!Files.deleteIfExists(file.toPath()))
            throw new IOException("Failed to delete file: " + file.getAbsolutePath());
        
        return result;
    }
    
    @Override
    @SuppressWarnings("rawtypes")
    public Map delete(String id) throws IOException {
        return cloudinary.uploader().destroy(id, ObjectUtils.emptyMap());
    }

    public File convert(MultipartFile multipartFile) throws IOException {
        File file = new File(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        FileOutputStream fo = new FileOutputStream(file);
        fo.write(multipartFile.getBytes());
        fo.close();
        return file;
    }
}
