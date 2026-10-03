package com.multi.shop.api.multi_shop_api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FakeMediaService extends Dispatcher {
    private static final Pattern FILENAME = Pattern.compile("filename=\"([^\"]*)\"");
    private static final ObjectMapper JSON = new ObjectMapper();

    private final MockWebServer server = new MockWebServer();
    private final Map<String, Image> images = new LinkedHashMap<>();
    private final List<String> deletedImageIds = new ArrayList<>();
    private int uploads;
    private boolean down;
    private boolean refuseConfirm;

    public record Image(String id, String name, String imageUrl, String imageId, @JsonIgnore String status) {
        Image with(String newName, String newStatus) {
            return new Image(id, newName, imageUrl, imageId, newStatus);
        }
    }

    public void start() {
        try {
            server.setDispatcher(this);
            server.start();
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    public String url() {
        return "http://localhost:" + server.getPort();
    }

    public synchronized void reset() {
        images.clear();
        deletedImageIds.clear();
        uploads = 0;
        down = false;
        refuseConfirm = false;
    }

    public synchronized void setDown(boolean down) {
        this.down = down;
    }

    public synchronized void setRefuseConfirm(boolean refuseConfirm) {
        this.refuseConfirm = refuseConfirm;
    }

    public synchronized List<String> deletedImageIds() {
        return List.copyOf(deletedImageIds);
    }

    public synchronized List<Image> images() {
        return List.copyOf(images.values());
    }

    public synchronized Optional<Image> image(String id) {
        return Optional.ofNullable(images.get(id));
    }

    @Override
    public synchronized MockResponse dispatch(RecordedRequest request) {
        if (down) return new MockResponse().setResponseCode(503);

        HttpUrl url = Objects.requireNonNull(request.getRequestUrl());
        List<String> path = url.pathSegments();
        String method = request.getMethod();

        try {
            if ("POST".equals(method) && path.equals(List.of("images"))) return upload(request);
            if ("GET".equals(method) && path.equals(List.of("images"))) return findAll(url.queryParameterValues("ids"));
            if ("POST".equals(method) && path.equals(List.of("images", "confirm"))) return confirm(request);
            if ("GET".equals(method) && path.size() == 2) return findOne(path.get(1));
            if ("PUT".equals(method) && path.size() == 3 && "name".equals(path.get(2))) return rename(path.get(1), request);
            if ("DELETE".equals(method) && path.size() == 2) return delete(path.get(1));
        } catch (IOException exception) {
            return new MockResponse().setResponseCode(400);
        }

        return new MockResponse().setResponseCode(404);
    }

    private MockResponse upload(RecordedRequest request) throws IOException {
        Matcher filename = FILENAME.matcher(request.getBody().readString(StandardCharsets.ISO_8859_1));
        String name = filename.find() ? new String(filename.group(1).getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8) : null;
        int number = ++uploads;
        Image image = new Image(
            UUID.randomUUID().toString(),
            name,
            "http://res.cloudinary.com/prueba/image/upload/foto-" + number + ".png",
            "foto-" + number,
            "PENDING");
        images.put(image.id(), image);
        return json(201, image);
    }

    private MockResponse findAll(List<String> ids) throws IOException {
        List<Image> found = ids.stream().filter(Objects::nonNull).map(images::get).filter(Objects::nonNull).toList();
        return json(200, found);
    }

    private MockResponse findOne(String id) throws IOException {
        Image image = images.get(id);
        return image == null ? new MockResponse().setResponseCode(404) : json(200, image);
    }

    private MockResponse rename(String id, RecordedRequest request) throws IOException {
        Image image = images.get(id);
        if (image == null) return new MockResponse().setResponseCode(404);

        String name = JSON.readTree(request.getBody().readUtf8()).get("name").asText();
        images.put(id, image.with(name, image.status()));
        return new MockResponse().setResponseCode(204);
    }

    private MockResponse confirm(RecordedRequest request) throws IOException {
        if (refuseConfirm) return new MockResponse().setResponseCode(500);

        List<String> missing = new ArrayList<>();
        for (JsonNode id : JSON.readTree(request.getBody().readUtf8()).get("ids")) {
            Image image = images.get(id.asText());
            if (image == null) missing.add(id.asText());
            else images.put(image.id(), image.with(image.name(), "CONFIRMED"));
        }
        return missing.isEmpty() ? new MockResponse().setResponseCode(204) : json(404, Map.of("missing", missing));
    }

    private MockResponse delete(String id) {
        Image image = images.remove(id);
        if (image != null) deletedImageIds.add(image.imageId());
        return new MockResponse().setResponseCode(204);
    }

    private static MockResponse json(int status, Object body) throws IOException {
        return new MockResponse()
            .setResponseCode(status)
            .setHeader("Content-Type", "application/json")
            .setBody(JSON.writeValueAsString(body));
    }
}
