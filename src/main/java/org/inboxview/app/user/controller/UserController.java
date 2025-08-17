package org.inboxview.app.user.controller;

import org.inboxview.app.user.dto.UserDto;
import org.inboxview.app.user.repository.projection.UserMailboxTransaction;
import org.inboxview.app.user.service.UserService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public Mono<UserDto> getUser() {
        return userService.getUser();
    }

    @GetMapping("/mailbox-transaction/{month}")
    public Flux<UserMailboxTransaction> getMailboxTransaction(@PathVariable("month") Integer month) {
        return userService.getMailboxTransactionByMonth(month);
    }
    
}
