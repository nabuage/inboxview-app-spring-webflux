package org.inboxview.app.user.service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.inboxview.app.config.JwtService;
import org.inboxview.app.error.InvalidRequest;
import org.inboxview.app.error.NotFoundException;
import org.inboxview.app.user.dto.AuthenticationRequestDto;
import org.inboxview.app.user.dto.AuthenticationResponseDto;
import org.inboxview.app.user.dto.RefreshTokenRequestDto;
import org.inboxview.app.user.entity.RefreshToken;
import org.inboxview.app.user.repository.RefreshTokenRepository;
import org.inboxview.app.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import static org.inboxview.app.error.ExceptionTextConstants.*;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    private static final String INVALID_CREDENTIALS = "Invalid credentials.";
    private static final String NOT_VERIFIED = "User is not verified.";

    @Value("${jwt.refresh-token-ttl}")
    private Duration ttl;

    public Mono<AuthenticationResponseDto> authenticate(
        final AuthenticationRequestDto request
    ) {
        final var authToken = UsernamePasswordAuthenticationToken
            .unauthenticated(request.username(), request.password());

        try {
            authenticationManager.authenticate(authToken);
        } catch (BadCredentialsException e) {
            return Mono.error(new BadCredentialsException(INVALID_CREDENTIALS));
        }

        return userRepository
            .findByUsername(request.username())
            .switchIfEmpty(Mono.error(new BadCredentialsException(INVALID_CREDENTIALS)))
            .filter(user -> user.getDateVerified() != null)
            .switchIfEmpty(Mono.error(new InvalidRequest(NOT_VERIFIED)))
            .flatMap(user -> {
                var accessToken = jwtService.generateToken(request.username());

                RefreshToken refreshToken = RefreshToken.builder()
                    .userId(user.getId())
                    .guid(UUID.randomUUID().toString())
                    .accessToken(accessToken)
                    .dateAdded(OffsetDateTime.now())
                    .expirationDate(OffsetDateTime.now().plus(ttl))
                    .build();
                
                return refreshTokenRepository
                    .save(refreshToken)
                    .flatMap(savedRefreshToken -> {
                        return Mono.just(
                                new AuthenticationResponseDto(savedRefreshToken.getAccessToken(), savedRefreshToken.getGuid())
                            );
                    });
            });
    }

    public Mono<AuthenticationResponseDto> refreshToken(
        final RefreshTokenRequestDto request
    ) {
        return refreshTokenRepository
            .findByGuidAndAccessTokenAndExpirationDateAfter(
                request.refreshToken(),
                request.accessToken(),
                OffsetDateTime.now()
            )
            .switchIfEmpty(Mono.error(new NotFoundException(INVALID_VERIFICATION_CODE)))
            .flatMap(rToken -> {
                return userRepository
                    .findById(rToken.getUserId())
                    .switchIfEmpty(Mono.error(new NotFoundException(INVALID_VERIFICATION_CODE)))
                    .flatMap(user -> {
                        var accessToken = jwtService.generateToken(user.getUsername());

                        rToken.setAccessToken(accessToken);
                        rToken.setExpirationDate(OffsetDateTime.now().plus(ttl));

                        return refreshTokenRepository
                            .save(rToken)
                            .flatMap(updatedToken -> {
                                return Mono.just(
                                        new AuthenticationResponseDto(
                                            updatedToken.getAccessToken(),
                                            request.refreshToken()
                                        )
                                    );
                            });
                    });
            });
    }

    @Transactional
    public Mono<Void> revokeRefreshToken(String refreshToken) {
        return refreshTokenRepository.deleteByGuid(refreshToken);
    }
}
