package org.inboxview.app.user.service;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.inboxview.app.error.DuplicateException;
import org.inboxview.app.user.dto.RegistrationRequestDto;
import org.inboxview.app.user.dto.UserDto;
import org.inboxview.app.user.entity.User;
import org.inboxview.app.user.mapper.UserMapper;
import org.inboxview.app.user.repository.UserRepository;
import org.inboxview.app.utils.DateUtil;
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
        return userRepository.existsByUsername(request.email())
            .flatMap(found -> {
                if (found) {
                    throw new DuplicateException(USERNAME_EXIST);
                }
                
                return userRepository.save(
                        User.builder()
                        .guid(UUID.randomUUID().toString())
                        .username(request.email())
                        .email(request.email())
                        .password(passwordEncoder.encode(request.password()))
                        .firstName(request.firstName())
                        .lastName(request.lastName())
                        .dateAdded(DateUtil.getCurrentDateTime())
                        .build()
                    )
                    .flatMap(registeredUser -> {
                        return verificationService.sendEmailVerification(registeredUser)
                            .filter(success -> success)
                            .switchIfEmpty(Mono.error(new RuntimeException(EMAIL_VERIFICATION_NOT_SENT)))
                            .map(success -> {
                                return userMapper.toDto(registeredUser);
                            });
                    });
            
            });
    }
    
}
