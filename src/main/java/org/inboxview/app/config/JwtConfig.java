package org.inboxview.app.config;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;

@Configuration
public class JwtConfig {
    private final String ALGORITHM = "RSA";
    private RSAPrivateKey privateKey;
    private RSAPublicKey publicKey;

    @Value("${jwt.public-key}")
    private String publicKeyString;
    @Value("${jwt.private-key}")
    private String privateKetString;
    @Value("${jwt.access-token-ttl}")
    private Duration accessTokenTtl;

    @Bean
    public JwtEncoder jwtEncoder() {
        final var jwk = new RSAKey
            .Builder(getPublicKey())
            .privateKey(getPrivateKey())
            .build();

        return new NimbusJwtEncoder(
            new ImmutableJWKSet<>(new JWKSet(jwk))
        );
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withPublicKey(getPublicKey()).build();
    }

    @Bean
    public JwtService jwtService(
        @Value("${spring.application.name}") final String appName,
        final JwtEncoder jwtEncoder,
        final JwtDecoder jwtDecoder
    ) {
        return new JwtService(appName, accessTokenTtl, jwtEncoder, jwtDecoder);
    }

    public RSAPrivateKey getPrivateKey() {
        if (privateKey == null) {            
            try {
                PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(
                    Base64.getDecoder()
                    .decode(
                        privateKetString.getBytes(StandardCharsets.UTF_8)
                    )
                );

                KeyFactory factory;
                factory = KeyFactory.getInstance(ALGORITHM);

                return (RSAPrivateKey) factory.generatePrivate(keySpec);
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            }
        }

        return privateKey;
  }

    public RSAPublicKey getPublicKey() {
        if (publicKey == null) {            
            try {
                byte[] data = Base64.getDecoder()
                .decode(
                    publicKeyString.getBytes(StandardCharsets.UTF_8)
                );

                X509EncodedKeySpec spec = new X509EncodedKeySpec(data);
                KeyFactory factory;

                factory = KeyFactory.getInstance(ALGORITHM);

                publicKey = (RSAPublicKey) factory.generatePublic(spec);
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            }
        }

        return publicKey;
        
    }
}
