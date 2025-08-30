package org.inboxview.app.user.mapper;

import org.inboxview.app.user.dto.UserDto;
import org.inboxview.app.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public User toEntity(UserDto userDto) {
        User user = new User();

        user.setGuid(userDto.id());
        user.setEmail(userDto.email());
        user.setUsername(userDto.email());
        user.setFirstName(user.getFirstName());
        user.setLastName(user.getLastName());
        user.setPhone(user.getPhone());

        return user;
    }

    public UserDto toDto(User user) {
        return UserDto.builder()
            .id(user.getGuid())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .phone(user.getPhone())
            .isVerified(user.getDateVerified() != null)
            .build();
    }
}
