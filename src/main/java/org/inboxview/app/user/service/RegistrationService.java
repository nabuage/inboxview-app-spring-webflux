package org.inboxview.app.user.service;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.inboxview.app.error.DuplicateException;
import org.inboxview.app.user.dto.RegistrationRequestDto;
import org.inboxview.app.user.dto.UserDto;
import org.inboxview.app.user.entity.User;
import org.inboxview.app.user.mapper.UserMapper;
import org.inboxview.app.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import static org.inboxview.app.error.ExceptionTextConstants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationService verificationService;
    private final UserMapper userMapper;

    public Mono<UserDto> register(RegistrationRequestDto request) {
        return userRepository.existsByUsername(request.username())
            .flatMap(found -> {
                if (found) {
                    throw new DuplicateException(USERNAME_EXIST);
                }
                
                return userRepository.save(
                        User.builder()
                        .guid(UUID.randomUUID().toString())
                        .username(request.username())
                        .email(request.email())
                        .password(passwordEncoder.encode(request.password()))
                        .firstName(request.firstName())
                        .lastName(request.lastName())
                        .dateAdded(OffsetDateTime.now())
                        .build()
                    )
                    .flatMap(registeredUser -> {
                        return verificationService.sendEmailVerification(registeredUser)
                            .map(success -> {
                                return userMapper.toDto(registeredUser);
                            });
                    })
                    .onErrorResume(e -> {
                        log.error("Error with registration.", e);
                        return Mono.error(e);
                    });
            
            });
    }
    
}
