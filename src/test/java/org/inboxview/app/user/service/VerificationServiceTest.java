package org.inboxview.app.user.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.inboxview.app.error.DuplicateException;
import org.inboxview.app.error.NotFoundException;
import org.inboxview.app.user.dto.UserDto;
import org.inboxview.app.user.entity.User;
import org.inboxview.app.user.entity.UserVerification;
import org.inboxview.app.user.mapper.UserMapper;
import org.inboxview.app.user.repository.UserRepository;
import org.inboxview.app.user.repository.UserVerificationRepository;
import org.inboxview.app.utils.DateUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class VerificationServiceTest {
    static final String FROM = "george@inboxview.com";
    static final String URL = "http://localhost:8080/";
    static final String SUBJECT = "Email verification";
    static final String BODY = "Here's your link to verify your email: %sapi/registration/email/verify?id=%s&code=%s";
    static final String USER_NOT_FOUND_ERROR = "User is not found.";
    static final String INVALID_CODE_ERROR = "Invalid code.";
    static final String ALREADY_VERIFIED_ERROR = "Email already verified.";

    @InjectMocks
    VerificationService verificationService;

    @Mock
    UserVerificationRepository userVerificationRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    MessageSenderService messageSenderService;

    @Spy
    UserMapper userMapper;
    
    User user;
    UserDto userDto;

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(verificationService, "FROM", FROM);
        ReflectionTestUtils.setField(verificationService, "url", URL);

        user = User.builder()
            .id(1L)
            .guid(UUID.randomUUID().toString())
            .username("username")
            .password("password")
            .email("george@inboxview.com")
            .firstName("firstname")
            .lastName("lastname")
            .build();

        userDto = UserDto.builder()
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .phone(user.getPhone())
            .isVerified(Boolean.FALSE)
            .build();
    }

    @Test
    public void testSendEmailVerificationReturnsSuccess() {
        var userId = 1L;
        var userVerification = UserVerification.builder()
            .userId(userId)
            .code(UUID.randomUUID().toString())
            .attemptCount(0L)
            .build();

        when(userVerificationRepository.save(any())).thenReturn(Mono.just(userVerification));
        when(userRepository.findById(anyLong())).thenReturn(Mono.just(user));
        when(messageSenderService.sendEmail(anyString(), anyString(), anyString())).thenReturn(Mono.just(Boolean.TRUE));

        var result = verificationService.sendEmailVerification(user);

        StepVerifier.create(result)
            .expectNextMatches(success -> {
                return success;
            })
            .verifyComplete();

        verify(userVerificationRepository, times(1)).save(any());
        verify(userRepository, times(1)).findById(anyLong());
        verify(messageSenderService, times(1)).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    public void testSendEmailVerificationReturnsFalse() {
        when(userRepository.findById(anyLong())).thenReturn(Mono.empty());

        var result = verificationService.sendEmailVerification(user);

        StepVerifier.create(result)
            .expectNextMatches(success -> {
                return !success;
            })
            .verifyComplete();

        verify(userRepository, times(1)).findById(anyLong());
        verify(userVerificationRepository, never()).save(any());
        verify(messageSenderService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    public void testVerifyEmailReturnsSuccess() {
        var userId = 1L;
        var userVerification = UserVerification.builder()
            .userId(userId)
            .code(UUID.randomUUID().toString())
            .attemptCount(0L)
            .dateAdded(DateUtil.getCurrentDateTime())
            .build();

        when(userRepository.findByGuid(anyString())).thenReturn(Mono.just(user));
        when(userVerificationRepository.findByUserId(anyLong())).thenReturn(Mono.just(userVerification));
        when(userVerificationRepository.save(any())).thenReturn(Mono.just(userVerification));
        when(userRepository.save(any())).thenReturn(Mono.just(user));
        
        var result = verificationService.verifyEmail(user.getGuid(), userVerification.getCode());

        StepVerifier.create(result)
            .expectNextMatches(dto -> {
                return dto.isVerified();
            })
            .verifyComplete();

        verify(userRepository, times(1)).findByGuid(anyString());
        verify(userVerificationRepository, times(1)).findByUserId(anyLong());
        verify(userVerificationRepository, times(1)).save(any());
        verify(userMapper, times(1)).toDto(any());
    }

    @Test
    public void testVerifyEmailReturnsUserNotFound() {
        var userId = 1L;
        var userVerification = UserVerification.builder()
            .userId(userId)
            .code(UUID.randomUUID().toString())
            .attemptCount(0L)
            .dateAdded(DateUtil.getCurrentDateTime())
            .build();

        when(userRepository.findByGuid(anyString())).thenReturn(Mono.empty());

        var result = verificationService.verifyEmail(user.getGuid(), userVerification.getCode());

        StepVerifier.create(result)
            .expectError(NotFoundException.class)
            .verify();

        verify(userRepository, times(1)).findByGuid(anyString());
        verify(userVerificationRepository, never()).findByUserId(anyLong());
        verify(userVerificationRepository, never()).save(any());
        verify(userMapper, never()).toDto(any());
    }

    @Test
    public void testVerifyEmailReturnsUserIdNotFound() {
        var userId = 1L;
        var userVerification = UserVerification.builder()
            .userId(userId)
            .code(UUID.randomUUID().toString())
            .attemptCount(0L)
            .dateAdded(DateUtil.getCurrentDateTime())
            .build();

        when(userRepository.findByGuid(anyString())).thenReturn(Mono.just(user));
        when(userVerificationRepository.findByUserId(anyLong())).thenReturn(Mono.empty());

        var result = verificationService.verifyEmail(user.getGuid(), userVerification.getCode());

        StepVerifier.create(result)
            .expectError(NotFoundException.class)
            .verify();

        verify(userRepository, times(1)).findByGuid(anyString());
        verify(userVerificationRepository, times(1)).findByUserId(anyLong());
        verify(userVerificationRepository, never()).save(any());
        verify(userMapper, never()).toDto(any());
    }

    @Test
    public void testVerifyEmailReturnsInvalidCodeMoreThanMaxAttemptCount() {
        var userId = 1L;
        var userVerification = UserVerification.builder()
            .userId(userId)
            .code(UUID.randomUUID().toString())
            .attemptCount(11L)
            .dateAdded(DateUtil.getCurrentDateTime())
            .build();

        when(userRepository.findByGuid(anyString())).thenReturn(Mono.just(user));
        when(userVerificationRepository.findByUserId(anyLong())).thenReturn(Mono.just(userVerification));
        when(userVerificationRepository.save(any())).thenReturn(Mono.just(userVerification));

        var result = verificationService.verifyEmail(user.getGuid(), userVerification.getCode());

        StepVerifier.create(result)
            .expectError(NotFoundException.class)
            .verify();

        verify(userRepository, times(1)).findByGuid(anyString());
        verify(userVerificationRepository, times(1)).findByUserId(anyLong());
        verify(userVerificationRepository, times(1)).save(any());
        verify(userMapper, never()).toDto(any());
    }

    @Test
    public void testVerifyEmailReturnsInvalidCodeMoreThanMaxSecondsExpiration() {
        var userId = 1L;
        var userVerification = UserVerification.builder()
            .userId(userId)
            .code(UUID.randomUUID().toString())
            .attemptCount(0L)
            .dateAdded(DateUtil.getCurrentDateTime().plusSeconds(86400L + 1000L))
            .build();

        when(userRepository.findByGuid(anyString())).thenReturn(Mono.just(user));
        when(userVerificationRepository.findByUserId(anyLong())).thenReturn(Mono.just(userVerification));
        when(userVerificationRepository.save(any())).thenReturn(Mono.just(userVerification));

        var result = verificationService.verifyEmail(user.getGuid(), userVerification.getCode());

        StepVerifier.create(result)
            .expectError(NotFoundException.class)
            .verify();

        verify(userRepository, times(1)).findByGuid(anyString());
        verify(userVerificationRepository, times(1)).findByUserId(anyLong());
        verify(userVerificationRepository, times(1)).save(any());
        verify(userMapper, never()).toDto(any());
    }

    @Test
    public void testResendEmailVerificationReturnsSuccess() {
        var userVerification = UserVerification.builder()
            .userId(user.getId())
            .code(UUID.randomUUID().toString())
            .attemptCount(0L)
            .build();

        when(userRepository.findByGuid(anyString())).thenReturn(Mono.just(user));
        when(userVerificationRepository.setDateDeletedByUserId(anyLong(), any())).thenReturn(Mono.empty());
        when(userVerificationRepository.save(any())).thenReturn(Mono.just(userVerification));
        when(userRepository.findById(anyLong())).thenReturn(Mono.just(user));
        when(messageSenderService.sendEmail(anyString(), anyString(), anyString())).thenReturn(Mono.just(Boolean.TRUE));

        var result = verificationService.resendEmailVerification(user.getGuid());

        StepVerifier.create(result)
            .expectNextCount(0)
            .verifyComplete();

        verify(userRepository, times(1)).findByGuid(anyString());
        verify(userVerificationRepository, times(1)).save(any());
        verify(userRepository, times(1)).findById(anyLong());
        verify(messageSenderService, times(1)).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    public void testResendEmailVerificationReturnsNotFoundException() {
        when(userRepository.findByGuid(anyString())).thenReturn(Mono.empty());

        var result = verificationService.resendEmailVerification(user.getGuid());

        StepVerifier.create(result)
            .expectError(NotFoundException.class)
            .verify();

        verify(userRepository, times(1)).findByGuid(anyString());
    }

    @Test
    public void testResendEmailVerificationReturnsDuplicateException() {
        user = User.builder()
            .id(1L)
            .guid(UUID.randomUUID().toString())
            .username("username")
            .password("password")
            .email("george@inboxview.com")
            .firstName("firstname")
            .lastName("lastname")
            .dateVerified(DateUtil.getCurrentDateTime())
            .build();

        when(userRepository.findByGuid(anyString())).thenReturn(Mono.just(user));

        var result = verificationService.resendEmailVerification(user.getGuid());

        StepVerifier.create(result)
            .expectError(DuplicateException.class)
            .verify();

        verify(userRepository, times(1)).findByGuid(anyString());
    }
}
