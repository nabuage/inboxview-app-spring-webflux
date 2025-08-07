package org.inboxview.app.user.repository;

import org.inboxview.app.user.entity.User;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends ReactiveCrudRepository<User, Long> {
    @Query("SELECT * FROM \"user\" WHERE username = $1 AND date_deleted IS NULL ORDER BY date_added DESC LIMIT 1")
    Mono<User> findByUsername(String username);
    
    @Query("SELECT * FROM \"user\" WHERE user_guid = $1 AND date_deleted IS NULL ORDER BY date_added DESC LIMIT 1")
    Mono<User> findByGuid(String guid);

    @Query("SELECT * FROM \"user\" WHERE user_guid = $1 AND password_reset_token = $2 AND password_reset_date_requested IS NOT NULL AND date_deleted IS NULL ORDER BY date_added DESC LIMIT 1")
    Mono<User> findByGuidAndPasswordResetToken(String guid, String token);
    
    Mono<Boolean> existsByUsername(String username);
    Mono<Boolean> existsByEmail(String email);
}
