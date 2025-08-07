package org.inboxview.app.user.dto;

import lombok.Builder;

@Builder
public record RegistrationRequestDto(
    String username,
    String email,
    String password,
    String firstName,
    String lastName,
    String phone
) {
    
}
