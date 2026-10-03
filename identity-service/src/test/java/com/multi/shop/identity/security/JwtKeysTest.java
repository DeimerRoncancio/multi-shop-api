package com.multi.shop.identity.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtKeysTest {

    @Test
    void readsAKeyPairAndSignsWithRs256() throws NoSuchAlgorithmException {
        KeyPair pair = rsa(2048);

        String token = Jwts.builder()
            .subject("ana@example.com")
            .signWith(JwtKeys.privateKey(base64(pair.getPrivate().getEncoded())))
            .compact();

        assertThat(Jwts.parser().verifyWith(JwtKeys.publicKey(base64(pair.getPublic().getEncoded()))).build()
            .parseSignedClaims(token).getPayload().getSubject()).isEqualTo("ana@example.com");
        assertThat(Jwts.parser().verifyWith(JwtKeys.publicKey(base64(pair.getPublic().getEncoded()))).build()
            .parseSignedClaims(token).getHeader().getAlgorithm()).isEqualTo("RS256");
    }

    @Test
    void rejectsTokensSignedWithAnotherKey() throws NoSuchAlgorithmException {
        KeyPair ours = rsa(2048);
        KeyPair theirs = rsa(2048);
        String forged = Jwts.builder().subject("admin@example.com").signWith(theirs.getPrivate()).compact();

        assertThatThrownBy(() -> Jwts.parser().verifyWith(JwtKeys.publicKey(base64(ours.getPublic().getEncoded()))).build()
            .parseSignedClaims(forged))
            .isInstanceOf(SignatureException.class);
    }

    @Test
    void explainsAMissingKey() {
        assertThatThrownBy(() -> JwtKeys.publicKey(null))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("JWT_PUBLIC_KEY is not set")
            .hasMessageContaining("openssl");
        assertThatThrownBy(() -> JwtKeys.privateKey(" "))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("JWT_PRIVATE_KEY is not set");
    }

    @Test
    void rejectsValuesThatAreNotKeys() {
        assertThatThrownBy(() -> JwtKeys.publicKey("no es base64!"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Base64");
        assertThatThrownBy(() -> JwtKeys.privateKey(base64("hola".getBytes())))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("PKCS#8");
    }

    @Test
    void rejectsWeakKeys() throws NoSuchAlgorithmException {
        KeyPair weak = rsa(1024);

        assertThatThrownBy(() -> JwtKeys.publicKey(base64(weak.getPublic().getEncoded())))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("2048");
    }

    private static KeyPair rsa(int bits) throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(bits);
        return generator.generateKeyPair();
    }

    private static String base64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }
}
