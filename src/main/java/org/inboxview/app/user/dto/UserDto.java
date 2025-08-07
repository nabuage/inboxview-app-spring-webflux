package org.inboxview.app.user.dto;

import lombok.Builder;

@Builder
public record UserDto(
    String email,
    String username,
    String firstName,
    String lastName,
    String phone,
    boolean isVerified
) {
    
}
