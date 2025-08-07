package org.inboxview.app.user.dto;

import lombok.Builder;

@Builder
public record RegistrationResponseDto(
    String id,
    String username,
    String email,
    String firstName,
    String lastName,
    String phone,
    boolean emailVerificationRequired
) {
    
}
