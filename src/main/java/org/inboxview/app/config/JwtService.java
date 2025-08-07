package org.inboxview.app.config;

import java.time.Duration;
import java.time.Instant;

import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtService {
    private final String issuer;
    private final Duration ttl;
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    public String generateToken(final String username) {
        final var claimsSet = JwtClaimsSet.builder()
            .subject(username)
            .issuer(issuer)
            .expiresAt(Instant.now().plus(ttl))
            .build();

        return jwtEncoder
            .encode(JwtEncoderParameters.from(claimsSet))
            .getTokenValue();
    }

    public String getAuthUser(HttpServletRequest request) {
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (token != null) {
            String user = jwtDecoder.decode(token).getSubject();

            if (user != null) {
                return user;
            }
        }

        return null;
    }
}
