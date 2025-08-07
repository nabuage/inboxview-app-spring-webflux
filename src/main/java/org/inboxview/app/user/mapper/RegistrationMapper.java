package org.inboxview.app.user.mapper;

import org.inboxview.app.user.dto.RegistrationRequestDto;
import org.inboxview.app.user.dto.RegistrationResponseDto;
import org.inboxview.app.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class RegistrationMapper {
    public User toEntity(RegistrationRequestDto request) {
        final var user = new User();

        user.setEmail(request.email());
        user.setUsername(request.username());
        user.setPassword(request.password());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPhone(request.phone());

        return user;
    }

    public RegistrationResponseDto toRegistrationResponse(final User user, boolean emailVerificationRequired) {
        return new RegistrationResponseDto(
            user.getGuid(),
            user.getUsername(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getPhone(),
            emailVerificationRequired
        );
    }
}
