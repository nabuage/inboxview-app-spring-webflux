package org.inboxview.app.user.dto;

public record VerifyRequestDto(
    String id,
    String code
) {
    
}
