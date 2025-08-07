package org.inboxview.app.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.OffsetDateTime;
import org.inboxview.app.error.InvalidRequest;
import org.inboxview.app.user.dto.PasswordResetRequestDto;
import org.inboxview.app.user.entity.User;
import org.inboxview.app.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class PasswordServiceTest {
    private static final String URL = "http://localhost:8080";
    private static final String PASSWORD_NOT_EQUAL_ERROR = "Password and Password Confirmation must be the same.";
    
    @InjectMocks
    PasswordService passwordService;

    @Mock
    UserRepository userRepository;

    @Mock
    MessageSenderService messageSenderService;

    @Mock
    PasswordEncoder passwordEncoder;

    User user;

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(passwordService, "url", URL);

        user = User.builder()
            .id(1L)
            .username("username")
            .password("password")
            .email("email@inboxview.com")
            .firstName("firstname")
            .lastName("lastname")
            .dateVerified(OffsetDateTime.now())
            .build();
    }

    @Test
    public void testEmailResetLinkIsSent() {
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Mono.just(user));
        when(userRepository.save(any())).thenReturn(Mono.just(user));
        when(messageSenderService.sendEmail(anyString(), anyString(), anyString())).thenReturn(Mono.empty());

        var result = passwordService.emailResetLink(user.getUsername());

        StepVerifier.create(result)
            .verifyComplete();

        verify(userRepository, times(1)).findByUsername(anyString());
        verify(userRepository, times(1)).save(any());
        verify(messageSenderService, times(1)).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    public void testEmailResetLinkIsNotSent() {
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Mono.empty());

        var result = passwordService.emailResetLink(user.getUsername());

        StepVerifier.create(result)
            .verifyComplete();

        verify(userRepository, times(1)).findByUsername(anyString());
        verify(userRepository, never()).save(any());
        verify(messageSenderService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    public void testResetIsSuccess() {
        var request = PasswordResetRequestDto.builder()
            .id("guid")
            .password("password")
            .passwordConfirmation("password")
            .username("username")
            .token("token")
            .build();
        var userPasswordReset = user = User.builder()
            .id(1L)
            .username("username")
            .password("password")
            .email("email@inboxview.com")
            .firstName("firstname")
            .lastName("lastname")
            .dateVerified(OffsetDateTime.now())
            .passwordResetCount(0L)
            .passwordResetDateRequested(OffsetDateTime.now())
            .build();
        var encodedPassword = "encoded-password";

        when(userRepository.findByGuidAndPasswordResetToken(anyString(), anyString())).thenReturn(Mono.just(userPasswordReset));
        when(userRepository.save(any())).thenReturn(Mono.just(user));
        when(passwordEncoder.encode(anyString())).thenReturn(encodedPassword);
        when(messageSenderService.sendEmail(anyString(), anyString(), anyString())).thenReturn(Mono.empty());

        var result = passwordService.reset(request);

        StepVerifier.create(result)
            .verifyComplete();

        verify(userRepository, times(1)).findByGuidAndPasswordResetToken(anyString(), anyString());
        verify(userRepository, times(1)).save(any());
        verify(passwordEncoder, times(1)).encode(anyString());
        verify(messageSenderService, times(1)).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    public void testResetIsOverPasswordResetCount() {
        var request = PasswordResetRequestDto.builder()
            .id("guid")
            .password("password")
            .passwordConfirmation("password")
            .username("username")
            .token("token")
            .build();
        var userPasswordReset = user = User.builder()
            .id(1L)
            .username("username")
            .password("password")
            .email("email@inboxview.com")
            .firstName("firstname")
            .lastName("lastname")
            .dateVerified(OffsetDateTime.now())
            .passwordResetCount(11L)
            .passwordResetDateRequested(OffsetDateTime.now())
            .build();

        when(userRepository.findByGuidAndPasswordResetToken(anyString(), anyString())).thenReturn(Mono.just(userPasswordReset));

        var result = passwordService.reset(request);

        StepVerifier.create(result)
            .verifyComplete();

        verify(userRepository, times(1)).findByGuidAndPasswordResetToken(anyString(), anyString());
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
        verify(messageSenderService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    public void testResetIsOverPasswordResetDateRequested() {
        var request = PasswordResetRequestDto.builder()
            .id("guid")
            .password("password")
            .passwordConfirmation("password")
            .username("username")
            .token("token")
            .build();
        var userPasswordReset = user = User.builder()
            .id(1L)
            .username("username")
            .password("password")
            .email("email@inboxview.com")
            .firstName("firstname")
            .lastName("lastname")
            .dateVerified(OffsetDateTime.now())
            .passwordResetCount(9L)
            .passwordResetDateRequested(OffsetDateTime.now().minus(Duration.ofMinutes(11)))
            .build();

        when(userRepository.findByGuidAndPasswordResetToken(anyString(), anyString())).thenReturn(Mono.just(userPasswordReset));

        var result = passwordService.reset(request);

        StepVerifier.create(result)
            .verifyComplete();

        verify(userRepository, times(1)).findByGuidAndPasswordResetToken(anyString(), anyString());
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
        verify(messageSenderService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    public void testResetIsFailure() {
        var request = PasswordResetRequestDto.builder()
            .id("guid")
            .password("password")
            .passwordConfirmation("password")
            .username("username")
            .token("token")
            .build();

        when(userRepository.findByGuidAndPasswordResetToken(anyString(), anyString())).thenReturn(Mono.empty());
        
        var result = passwordService.reset(request);

        StepVerifier.create(result)
            .verifyComplete();

        verify(userRepository, times(1)).findByGuidAndPasswordResetToken(anyString(), anyString());
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
        verify(messageSenderService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    public void testResetIsPasswordNotEqualPasswordConfirmation() {
        var request = PasswordResetRequestDto.builder()
            .id("guid")
            .password("password")
            .passwordConfirmation("password1")
            .username("username")
            .token("token")
            .build();

        var result = passwordService.reset(request);

        StepVerifier.create(result)
            .expectError(InvalidRequest.class)
            .verify();

        verify(userRepository, never()).findByGuidAndPasswordResetToken(anyString(), anyString());
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
        verify(messageSenderService, never()).sendEmail(anyString(), anyString(), anyString());
    }
}
