package org.inboxview.app.user.repository;

import java.time.OffsetDateTime;
import org.inboxview.app.user.entity.UserVerification;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;

@Repository
public interface UserVerificationRepository extends ReactiveCrudRepository<UserVerification, Long> {
    @Query("SELECT * FROM user_verification WHERE user_id = $1 AND date_deleted IS NULL ORDER BY date_added DESC LIMIT 1")
    Mono<UserVerification> findByUserId(Long userId);

    @Modifying
    @Query("UPDATE user_verification SET date_deleted = $2 WHERE user_id = $1 AND date_deleted IS NULL")
    Mono<Void> setDateDeletedByUserId(Long userId, OffsetDateTime dateDeleted);
}