package org.inboxview.app.user.repository;

import java.time.OffsetDateTime;
import org.inboxview.app.user.entity.RefreshToken;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;

@Repository
public interface RefreshTokenRepository extends ReactiveCrudRepository<RefreshToken, Long> {
    Mono<RefreshToken> findByGuidAndAccessTokenAndExpirationDateAfter(String guid, String accessToken, OffsetDateTime expirationDate);

    Mono<Void> deleteByGuid(String guid);

    Mono<Void> deleteByAccessToken(String accessToken);
}
