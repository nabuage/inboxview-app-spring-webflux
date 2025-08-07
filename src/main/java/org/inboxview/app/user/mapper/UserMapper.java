package org.inboxview.app.user.mapper;

import org.inboxview.app.user.dto.UserDto;
import org.inboxview.app.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public User toEntity(UserDto userDto) {
        User user = new User();

        user.setEmail(userDto.email());
        user.setUsername(userDto.username());

        return user;
    }

    public UserDto toDto(User user) {
        return new UserDto(
            user.getEmail(),
            user.getUsername(),
            user.getFirstName(),
            user.getLastName(),
            user.getPhone(),
            user.getDateVerified() != null
        );
    }
}
