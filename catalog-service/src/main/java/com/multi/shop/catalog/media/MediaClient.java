package com.multi.shop.catalog.media;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@FeignClient(name = "media", url = "${media.service.url}")
public interface MediaClient {
    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    StoredImage upload(@RequestPart("file") MultipartFile file);

    @GetMapping("/images/{id}")
    StoredImage findOne(@PathVariable("id") String id);

    @GetMapping("/images")
    List<StoredImage> findAll(@RequestParam("ids") List<String> ids);

    @PutMapping("/images/{id}/name")
    void rename(@PathVariable("id") String id, @RequestBody Map<String, String> body);

    @PostMapping("/images/confirm")
    void confirm(@RequestBody Map<String, List<String>> body);

    @DeleteMapping("/images/{id}")
    void delete(@PathVariable("id") String id);
}
