package org.inboxview.app.user.dto;

import lombok.Builder;

@Builder
public record RefreshTokenRequestDto(
    String refreshToken,
    String accessToken
) {}
