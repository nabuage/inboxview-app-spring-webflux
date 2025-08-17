package org.inboxview.app.user.service;

import org.inboxview.app.user.dto.UserDto;
import org.inboxview.app.user.mapper.UserMapper;
import org.inboxview.app.user.repository.UserRepository;
import org.inboxview.app.user.repository.projection.UserMailboxTransaction;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.inboxview.app.error.ExceptionTextConstants.*;

import org.inboxview.app.config.IAuthentication;
import org.inboxview.app.error.NotFoundException;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository; 
    private final UserMapper userMapper;
    private final IAuthentication authentication;

    public Mono<UserDto> getUser() {        
        return userRepository
            .findByUsername(authentication.getAuthentication().getName())
            .switchIfEmpty(Mono.error(new NotFoundException(USER_NOT_FOUND)))
            .map(user -> {
                return userMapper.toDto(user);
            });
    }

    public Flux<UserMailboxTransaction> getMailboxTransactionByMonth(Integer month) {
        return userRepository
            .getByUsernameAndMonth(authentication.getAuthentication().getName(), month);
    }
}
