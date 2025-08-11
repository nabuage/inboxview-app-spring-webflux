package org.inboxview.app.user.dto;

import lombok.Builder;

@Builder
public record AuthenticationRequestDto(
    String email,
    String password
) {
    
}
