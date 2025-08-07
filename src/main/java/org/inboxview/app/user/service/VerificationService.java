package org.inboxview.app.user.service;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.inboxview.app.error.NotFoundException;
import org.inboxview.app.user.dto.UserDto;
import org.inboxview.app.user.entity.User;
import org.inboxview.app.user.entity.UserVerification;
import org.inboxview.app.user.mapper.UserMapper;
import org.inboxview.app.user.repository.UserRepository;
import org.inboxview.app.user.repository.UserVerificationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import static org.inboxview.app.error.ExceptionTextConstants.*;

@Service
@RequiredArgsConstructor
public class VerificationService {
    private final UserRepository userRepository;
    private final UserVerificationRepository userVerificationRepository;
    private final UserMapper userMapper;
    private final MessageSenderService messageSenderService;
    private static final String SUBJECT = "Email verification";
    private static final String BODY = "Here's your link to verify your email: %sapi/registration/email/verify?id=%s&code=%s";
    private static final int MAX_ATTEMPT_COUNT = 10;
    private static final Long MAX_SECONDS_EXPIRATION = 86400L;

    @Value("${app.from-email}")
    private String FROM;

    @Value("${app.url}")
    private String url;

    // @Async
    public Mono<Boolean> sendEmailVerification(User user) {
        return generateEmailToken(user.getId())
            .flatMap(code -> {
                var email = user.getEmail();
                var guid = user.getGuid();

                return messageSenderService.sendEmail(email, SUBJECT, BODY.formatted(url, guid, code))
                    .flatMap(sent -> {
                        return Mono.just(sent);
                    });
            });
    }

    private Mono<String> generateEmailToken(Long userId) {
        UserVerification verification = new UserVerification();
        verification.setUserId(userId);
        verification.setCode(UUID.randomUUID().toString());
        verification.setAttemptCount(0L);
        verification.setDateAdded(OffsetDateTime.now());

        return userVerificationRepository
            .save(verification)
            .map(v -> {
                return v.getCode();
            });
    }

    @Transactional
    public Mono<UserDto> verifyEmail(String userGuid, String code) {
        return userRepository
            .findByGuid(userGuid)
            .switchIfEmpty(Mono.error(new NotFoundException(USER_NOT_FOUND)))
            .flatMap(user -> {
                return userVerificationRepository
                    .findByUserId(user.getId())
                    .switchIfEmpty(Mono.error(new NotFoundException(INVALID_VERIFICATION_CODE)))
                    .flatMap(verification -> {
                        OffsetDateTime dateVerified = OffsetDateTime.now();
                        Long secondsBetweenDateAddedAndNow = ChronoUnit.SECONDS.between(dateVerified, verification.getDateAdded());

                        if (verification.getDateVerified() == null &&
                            verification.getAttemptCount() <= MAX_ATTEMPT_COUNT &&
                            verification.getCode().equals(code) &&
                            secondsBetweenDateAddedAndNow <= MAX_SECONDS_EXPIRATION
                        ) {
                            
                            verification.setDateVerified(dateVerified);
                            var saveVerification = userVerificationRepository.save(verification);

                            user.setDateVerified(dateVerified);
                            user.setDateUpdated(dateVerified);
                            var saveUser = userRepository.save(user);

                            return saveVerification.zipWith(saveUser).flatMap(tuple -> {
                                return Mono.just(userMapper.toDto(tuple.getT2()));
                            });
                        }
                        else {
                            verification.setAttemptCount(verification.getAttemptCount() + 1);

                            return userVerificationRepository.save(verification).flatMap(v -> {
                                return Mono.error(new NotFoundException(INVALID_VERIFICATION_CODE));
                            });
                        }                        
                    });
            });
    }

    @Transactional
    public Mono<Void> resendEmailVerification(String userGuid) {
        return userRepository.findByGuid(userGuid)
            .switchIfEmpty(Mono.error(new NotFoundException(USER_NOT_FOUND)))
            .filter(user -> user.getDateVerified() == null)
            .switchIfEmpty(Mono.error(new NotFoundException(USER_ALREADY_VERIFIED)))
            .flatMap(user -> {
                return userVerificationRepository
                    .setDateDeletedByUserId(user.getId(), OffsetDateTime.now())
                    .then(sendEmailVerification(user))
                    .then();
            });
    }
}
