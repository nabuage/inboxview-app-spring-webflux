package org.inboxview.app.user.dto;

import lombok.Builder;

@Builder
public record AuthenticationResponseDto(
    String accessToken,
    String refreshToken
) {
    
}
