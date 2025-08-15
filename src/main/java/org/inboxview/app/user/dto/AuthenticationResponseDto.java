package org.inboxview.app.user.dto;

import java.time.OffsetDateTime;

import lombok.Builder;

@Builder
public record AuthenticationResponseDto(
    String accessToken,
    String refreshToken,
    OffsetDateTime expireAt
) {
    
}
