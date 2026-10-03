package com.multi.shop.media.images;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/images")
public class ImageController {
    private final ImageService service;

    public ImageController(ImageService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ImageResponse> upload(@RequestPart("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.upload(file));
    }

    @GetMapping
    public List<ImageResponse> findAll(@RequestParam(name = "ids", required = false) List<String> ids) {
        return service.findAll(ids);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ImageResponse> findOne(@PathVariable String id) {
        return service.findOne(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/name")
    public ResponseEntity<Void> rename(@PathVariable String id, @Valid @RequestBody RenameRequest request) {
        return service.rename(id, request.name())
            ? ResponseEntity.noContent().build()
            : ResponseEntity.notFound().build();
    }

    @PostMapping("/confirm")
    public ResponseEntity<Map<String, Set<String>>> confirm(@Valid @RequestBody ConfirmRequest request) {
        Set<String> missing = service.confirm(request.ids());
        return missing.isEmpty()
            ? ResponseEntity.noContent().build()
            : ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("missing", missing));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
