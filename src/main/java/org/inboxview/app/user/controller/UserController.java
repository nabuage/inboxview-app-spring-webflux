package org.inboxview.app.user.controller;

import org.inboxview.app.user.dto.UserDto;
import org.inboxview.app.user.service.UserService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public Mono<UserDto> getUser(
        final Authentication authentication
    ) {
        return userService.getByUsername(authentication.getName());
    }
}
