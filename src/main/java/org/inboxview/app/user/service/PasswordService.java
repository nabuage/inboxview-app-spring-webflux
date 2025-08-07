package org.inboxview.app.user.service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.inboxview.app.error.InvalidRequest;
import org.inboxview.app.user.dto.PasswordResetRequestDto;
import org.inboxview.app.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class PasswordService {
    private static final String SUBJECT = "Password Reset";
    private static final String BODY = "Here's your link to reset your password: %sapi/password/reset?id=%s&code=%s";
    private static final String SUBJECT_VERIFY_PASSWORD_RESET = "Password Reset Confirmation";
    private static final String BODY_VERIFY_PASSWORD_RESET = "Your password was reset successfully.";
    private static final String PASSWORD_NOT_EQUAL_ERROR = "Password and Password Confirmation must be the same.";
    private static final int MAX_COUNT = 10;
    private static final long MAX_MINNUTES = 10;
    private final UserRepository userRepository;
    private final MessageSenderService messageSenderService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.url}")
    private String url;

    public Mono<Void> emailResetLink(String username) {
        return userRepository.findByUsername(username)
            .flatMap(user -> {
                user.setPasswordResetToken(UUID.randomUUID().toString());
                user.setPasswordResetDateRequested(OffsetDateTime.now());
                user.setPasswordResetCount(0L);

                return userRepository
                    .save(user)
                    .flatMap(savedUser -> {
                        return messageSenderService
                            .sendEmail(
                                savedUser.getEmail(),
                                SUBJECT,
                                BODY.formatted(url, savedUser.getGuid(), savedUser.getPasswordResetToken())
                            )
                            .flatMap(sent -> {
                                return Mono.empty();
                            });
                    });
            });
    }

    public Mono<Void> reset(PasswordResetRequestDto request) {
        if (!request.password().equals(request.passwordConfirmation())) {
            return Mono.error(new InvalidRequest(PASSWORD_NOT_EQUAL_ERROR));
        }

        return userRepository
            .findByGuidAndPasswordResetToken(request.id(), request.token())
            .flatMap(user -> {
                if (user.getPasswordResetCount() != null &&
                    user.getPasswordResetCount() <= MAX_COUNT &&
                    user.getPasswordResetDateRequested() != null &&
                    user.getPasswordResetDateRequested().plus(Duration.ofMinutes(MAX_MINNUTES)).isAfter(OffsetDateTime.now())
                ) {
                    user.setPassword(passwordEncoder.encode(request.password()));
                    user.setPasswordDateReset(OffsetDateTime.now());

                    return userRepository
                        .save(user)
                        .flatMap(savedUser -> {
                            return messageSenderService
                                .sendEmail(
                                    user.getEmail(),
                                    SUBJECT_VERIFY_PASSWORD_RESET,
                                    BODY_VERIFY_PASSWORD_RESET
                                )
                                .flatMap(sent -> {
                                    return Mono.empty();
                                });
                        });                    
                }

                return Mono.empty();
            });
    }
}
