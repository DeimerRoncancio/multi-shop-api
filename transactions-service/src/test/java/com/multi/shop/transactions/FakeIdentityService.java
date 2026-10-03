package com.multi.shop.transactions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.multi.shop.transactions.identity.Account;
import io.jsonwebtoken.Jwts;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class FakeIdentityService extends Dispatcher {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final PrivateKey TEST_PRIVATE_KEY = testPrivateKey();

    private final MockWebServer server = new MockWebServer();
    private final List<Account> accounts = new ArrayList<>();
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
        accounts.clear();
        down = false;
    }

    public synchronized void addAccount(String name, String email, Long phoneNumber) {
        accounts.add(new Account(name, email, phoneNumber));
    }

    public synchronized void setDown(boolean down) {
        this.down = down;
    }

    public static String userToken(String identity) {
        return token(identity, "[{\"authority\":\"ROLE_USER\"}]");
    }

    @Override
    public synchronized MockResponse dispatch(RecordedRequest request) {
        if (down) return new MockResponse().setResponseCode(503);

        HttpUrl url = Objects.requireNonNull(request.getRequestUrl());
        List<String> path = url.pathSegments();

        try {
            if (path.equals(List.of("internal", "accounts"))) {
                String identity = url.queryParameter("identity");
                String email = url.queryParameter("email");
                Optional<Account> account = accounts.stream()
                    .filter(found -> identity != null
                        ? identity.equals(found.email()) || identity.equals(String.valueOf(found.phoneNumber()))
                        : email != null && email.equals(found.email()))
                    .findFirst();
                return account.isPresent() ? json(account.get()) : new MockResponse().setResponseCode(404);
            }
        } catch (IOException exception) {
            return new MockResponse().setResponseCode(500);
        }

        return new MockResponse().setResponseCode(404);
    }

    private static String token(String identity, String authorities) {
        return Jwts.builder()
            .subject(identity)
            .claim("authorities", authorities)
            .expiration(new Date(System.currentTimeMillis() + 3600000))
            .issuedAt(new Date())
            .signWith(TEST_PRIVATE_KEY)
            .compact();
    }

    private static PrivateKey testPrivateKey() {
        try {
            byte[] der = Base64.getDecoder().decode(System.getenv("JWT_TEST_PRIVATE_KEY"));
            return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(der));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static MockResponse json(Object body) throws IOException {
        return new MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(JSON.writeValueAsString(body));
    }
}
