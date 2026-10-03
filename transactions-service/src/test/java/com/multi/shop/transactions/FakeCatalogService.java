package com.multi.shop.transactions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.multi.shop.transactions.catalog.CatalogProduct;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class FakeCatalogService extends Dispatcher {
    private static final ObjectMapper JSON = new ObjectMapper();

    private final MockWebServer server = new MockWebServer();
    private final Map<String, CatalogProduct> products = new LinkedHashMap<>();
    private boolean down;

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
        products.clear();
        down = false;
    }

    public synchronized CatalogProduct addProduct(String name, String description, long price) {
        CatalogProduct product = new CatalogProduct(UUID.randomUUID().toString(), name, description, price);
        products.put(product.id(), product);
        return product;
    }

    public synchronized void changePrice(String id, long price) {
        CatalogProduct product = products.get(id);
        products.put(id, new CatalogProduct(id, product.productName(), product.description(), price));
    }

    public synchronized void setDown(boolean down) {
        this.down = down;
    }

    @Override
    public synchronized MockResponse dispatch(RecordedRequest request) {
        if (down) return new MockResponse().setResponseCode(503);

        HttpUrl url = Objects.requireNonNull(request.getRequestUrl());
        List<String> path = url.pathSegments();

        try {
            if (path.equals(List.of("internal", "products")))
                return json(url.queryParameterValues("ids").stream().map(products::get).filter(Objects::nonNull).toList());
            if (path.size() == 3 && path.subList(0, 2).equals(List.of("internal", "products"))) {
                CatalogProduct product = products.get(path.get(2));
                return product == null ? new MockResponse().setResponseCode(404) : json(product);
            }
        } catch (IOException exception) {
            return new MockResponse().setResponseCode(500);
        }

        return new MockResponse().setResponseCode(404);
    }

    private static MockResponse json(Object body) throws IOException {
        return new MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(JSON.writeValueAsString(body));
    }
}
