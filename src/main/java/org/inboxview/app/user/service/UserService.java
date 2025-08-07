package org.inboxview.app.user.service;

import org.inboxview.app.user.dto.UserDto;
import org.inboxview.app.user.mapper.UserMapper;
import org.inboxview.app.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import static org.inboxview.app.error.ExceptionTextConstants.*;

import org.inboxview.app.error.NotFoundException;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository; 
    private final UserMapper userMapper;   

    public Mono<UserDto> getByUsername(String username) {
        return userRepository
            .findByUsername(username)
            .switchIfEmpty(Mono.error(new NotFoundException(USER_NOT_FOUND)))
            .map(user -> {
                return userMapper.toDto(user);
            });
    }
}
