package org.inboxview.app.user.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.inboxview.app.config.JwtService;
import org.inboxview.app.error.InvalidRequest;
import org.inboxview.app.error.NotFoundException;
import org.inboxview.app.user.dto.AuthenticationRequestDto;
import org.inboxview.app.user.dto.RefreshTokenRequestDto;
import org.inboxview.app.user.entity.RefreshToken;
import org.inboxview.app.user.entity.User;
import org.inboxview.app.user.repository.RefreshTokenRepository;
import org.inboxview.app.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {
    private static final String jwtToken = "jwt-token";
    private static final String BAD_CREDENTIALS_EXCEPTION = "Invalid credentials.";
    private static final String NOT_VERIFIED = "User is not verified.";

    @InjectMocks
    private AuthenticationService authenticationService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    JwtService jwtService;

    @Mock
    Authentication authentication;

    @Mock
    UserRepository userRepository;

    @Mock
    RefreshTokenRepository refreshTokenRepository;

    User user;
    AuthenticationRequestDto request;

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(authenticationService, "ttl", Duration.ofDays(1));

        user = User.builder()
            .id(1L)
            .username("username")
            .password("password")
            .email("email@inboxview.com")
            .firstName("firstname")
            .lastName("lastname")
            .dateVerified(OffsetDateTime.now())
            .build();

        request = AuthenticationRequestDto.builder()
            .username("username")
            .password("password")
            .build();
    }

    @Test
    public void testAuthenticateReturnsSuccess() {
        String refreshTokenGuid = UUID.randomUUID().toString();
        var authToken = UsernamePasswordAuthenticationToken
            .unauthenticated(request.username(), request.password());
        RefreshToken refreshToken = RefreshToken.builder()
            .accessToken(jwtToken)
            .guid(refreshTokenGuid)
            .build();

        when(authenticationManager.authenticate(authToken)).thenReturn(authentication);
        when(jwtService.generateToken(request.username())).thenReturn(jwtToken);
        when(userRepository.findByUsername(request.username())).thenReturn(Mono.just(user));
        when(refreshTokenRepository.save(any())).thenReturn(Mono.just(refreshToken));

        var result = authenticationService.authenticate(request);

        StepVerifier.create(result)
            .expectNextMatches(requestDto -> {
                assertThat(requestDto.accessToken()).isEqualTo(jwtToken);
                assertThat(requestDto.refreshToken()).isEqualTo(refreshTokenGuid);
                return Boolean.TRUE;
            })
            .verifyComplete();

        verify(authenticationManager, times(1)).authenticate(any());
        verify(jwtService, times(1)).generateToken(anyString());
        verify(refreshTokenRepository, times(1)).save(any());
    }

    @Test
    public void testAuthenticateReturnsInvalidPasswordBadCredentialsException() {
        var authToken = UsernamePasswordAuthenticationToken
            .unauthenticated(request.username(), request.password());

        when(authenticationManager.authenticate(authToken)).thenThrow(new BadCredentialsException(BAD_CREDENTIALS_EXCEPTION));

        var result = authenticationService.authenticate(request);

        StepVerifier.create(result)
            .expectError(BadCredentialsException.class)
            .verify();

        verify(authenticationManager, times(1)).authenticate(any());    
    }

    @Test
    public void testAuthenticateReturnsBadCredentialsException() {
        var authToken = UsernamePasswordAuthenticationToken
            .unauthenticated(request.username(), request.password());

        when(authenticationManager.authenticate(authToken)).thenReturn(authentication);
        when(userRepository.findByUsername(request.username())).thenReturn(Mono.empty());

        var result = authenticationService.authenticate(request);

        StepVerifier.create(result)
            .expectError(BadCredentialsException.class)
            .verify();

        verify(authenticationManager, times(1)).authenticate(any());
        verify(authenticationManager, times(1)).authenticate(any());
        verify(userRepository, times(1)).findByUsername(any());
    }

    @Test
    public void testAuthenticateReturnsUnverifiedUserInvalidRequestException() {
        var authToken = UsernamePasswordAuthenticationToken
            .unauthenticated(request.username(), request.password());
        var unverifiedUser = User.builder()
            .id(1L)
            .username("username")
            .password("password")
            .email("email@inboxview.com")
            .firstName("firstname")
            .lastName("lastname")
            .build();

        when(authenticationManager.authenticate(authToken)).thenReturn(authentication);
        when(userRepository.findByUsername(request.username())).thenReturn(Mono.just(unverifiedUser));

        var result = authenticationService.authenticate(request);

        StepVerifier.create(result)
            .expectError(InvalidRequest.class)
            .verify();

        verify(authenticationManager, times(1)).authenticate(any());
    }

    @Test
    public void testRefreshTokenReturnsSuccess() {
        RefreshToken refreshToken = RefreshToken.builder()
            .userId(user.getId())
            .guid(UUID.randomUUID().toString())
            .accessToken(jwtToken)
            .dateAdded(OffsetDateTime.now())
            .expirationDate(OffsetDateTime.now().plus(Duration.ofDays(1)))
            .build();
        var refreshTokenRequestDto = RefreshTokenRequestDto.builder()
            .accessToken(jwtToken)
            .refreshToken(refreshToken.getGuid())
            .build();

        when(refreshTokenRepository.findByGuidAndAccessTokenAndExpirationDateAfter(any(), any(), any())).thenReturn(Mono.just(refreshToken));
        when(userRepository.findById(anyLong())).thenReturn(Mono.just(user));
        when(jwtService.generateToken(request.username())).thenReturn(jwtToken);
        when(refreshTokenRepository.save(any())).thenReturn(Mono.just(refreshToken));
        
        var result = authenticationService.refreshToken(refreshTokenRequestDto);

        StepVerifier.create(result)
            .expectNextMatches(requestDto -> {
                assertThat(requestDto.accessToken()).isEqualTo(jwtToken);
                assertThat(requestDto.refreshToken()).isEqualTo(refreshToken.getGuid());
                return Boolean.TRUE;
            })
            .verifyComplete();

        verify(jwtService, times(1)).generateToken(any());
    }

    @Test
    public void testRefreshTokenReturnsBadCredentialsExceptionOnExpiredToken() {
        RefreshToken refreshToken = RefreshToken.builder()
            .userId(user.getId())
            .guid(UUID.randomUUID().toString())
            .accessToken(jwtToken)
            .dateAdded(OffsetDateTime.now())
            .expirationDate(OffsetDateTime.now().plus(Duration.ofDays(1)))
            .build();
        var refreshTokenRequestDto = RefreshTokenRequestDto.builder()
            .accessToken(jwtToken)
            .refreshToken(refreshToken.getGuid())
            .build();

        when(refreshTokenRepository.findByGuidAndAccessTokenAndExpirationDateAfter(any(), any(), any())).thenReturn(Mono.empty());

        var result = authenticationService.refreshToken(refreshTokenRequestDto);

        StepVerifier.create(result)
            .expectError(NotFoundException.class)
            .verify();

        verify(refreshTokenRepository, times(1)).findByGuidAndAccessTokenAndExpirationDateAfter(any(), any(), any());
    }

    @Test
    public void testRefreshTokenReturnsBadCredentialsExceptionOnFindById() {
        RefreshToken refreshToken = RefreshToken.builder()
            .userId(user.getId())
            .guid(UUID.randomUUID().toString())
            .dateAdded(OffsetDateTime.now())
            .expirationDate(OffsetDateTime.now().plus(Duration.ofDays(1)))
            .build();
        var refreshTokenRequestDto = RefreshTokenRequestDto.builder()
            .accessToken(jwtToken)
            .refreshToken(refreshToken.getGuid())
            .build();

        when(refreshTokenRepository.findByGuidAndAccessTokenAndExpirationDateAfter(any(), any(), any())).thenReturn(Mono.just(refreshToken));
        when(userRepository.findById(anyLong())).thenReturn(Mono.empty());

        var result = authenticationService.refreshToken(refreshTokenRequestDto);

        StepVerifier.create(result)
            .expectError(NotFoundException.class)
            .verify();

        verify(userRepository, times(1)).findById(anyLong());
    }

    @Test
    public void testRevokeRefreshToken() {
        String refreshToken = UUID.randomUUID().toString();

        when(refreshTokenRepository.deleteByGuid(anyString())).thenReturn(Mono.empty());

        var result = authenticationService.revokeRefreshToken(refreshToken);

        StepVerifier.create(result)
            .expectNextCount(0)
            .verifyComplete();

        verify(refreshTokenRepository, times(1)).deleteByGuid(refreshToken);
    }
}
