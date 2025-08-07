package org.inboxview.app.user.service;

import org.inboxview.app.user.repository.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import static org.inboxview.app.error.ExceptionTextConstants.*;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository
            .findByUsername(username)
            .switchIfEmpty(Mono.error(new UsernameNotFoundException(USER_NOT_FOUND)))
            .map(user -> {
                return User.builder()
                    .username(user.getUsername())
                    .password(user.getPassword())
                    .build();
            }).block();
    }    
}
