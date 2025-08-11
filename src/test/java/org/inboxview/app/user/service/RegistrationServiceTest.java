package org.inboxview.app.user.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.inboxview.app.error.DuplicateException;
import org.inboxview.app.user.dto.RegistrationRequestDto;
import org.inboxview.app.user.dto.UserDto;
import org.inboxview.app.user.entity.User;
import org.inboxview.app.user.mapper.UserMapper;
import org.inboxview.app.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class RegistrationServiceTest {
    @InjectMocks
    RegistrationService registrationService;

    @Mock
    VerificationService verificationService;

    @Mock
    UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    UserMapper userMapper;

    User user;
    UserDto userDto;
    RegistrationRequestDto request;

    @BeforeEach
    public void setup() {
        request = RegistrationRequestDto.builder()
            .password("password")
            .email("email@inboxview.com")
            .firstName("firstname")
            .lastName("lastname")
            .build();

        user = User.builder()
            .id(1L)
            .username(request.email())
            .password(request.password())
            .email(request.email())
            .firstName(request.firstName())
            .lastName(request.lastName())
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
    public void testRegisterReturnsSuccess() {
        when(userRepository.existsByUsername(anyString())).thenReturn(Mono.just(Boolean.FALSE));
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(user));
        when(verificationService.sendEmailVerification(any())).thenReturn(Mono.just(Boolean.TRUE));
        when(passwordEncoder.encode(user.getPassword())).thenReturn("encoded-password");
        when(userMapper.toDto(user)).thenReturn(userDto);

        var result = registrationService.register(request);

        StepVerifier.create(result)
            .expectNextMatches(dto -> {
                return Boolean.TRUE;
            })
            .verifyComplete();

        verify(userRepository, times(1)).existsByUsername(anyString());
        verify(userRepository, times(1)).save(any());
        verify(verificationService, times(1)).sendEmailVerification(any());
        verify(userMapper, times(1)).toDto(any());
    }

    @Test
    public void testRegisterReturnsRuntimeExceptionEmailVerification() {
        when(userRepository.existsByUsername(anyString())).thenReturn(Mono.just(Boolean.FALSE));
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(user));
        when(verificationService.sendEmailVerification(any())).thenReturn(Mono.just(Boolean.FALSE));
        when(passwordEncoder.encode(user.getPassword())).thenReturn("encoded-password");
        
        var result = registrationService.register(request);

        StepVerifier.create(result)
            .expectError(RuntimeException.class)
            .verify();

        verify(userRepository, times(1)).existsByUsername(anyString());
        verify(userRepository, times(1)).save(any());
        verify(verificationService, times(1)).sendEmailVerification(any());
        verify(userMapper, never()).toDto(any());
    }

    @Test
    public void testRegisterReturnsDuplicateException() {
        when(userRepository.existsByUsername(anyString())).thenReturn(Mono.just(Boolean.TRUE));

        var result = registrationService.register(request);

        StepVerifier.create(result)
            .expectError(DuplicateException.class)
            .verify();

        verify(userRepository, times(1)).existsByUsername(anyString());
        verify(userRepository, never()).save(any());
        verify(verificationService, never()).sendEmailVerification(any());
        verify(userMapper, never()).toDto(any());
    }
    
}
