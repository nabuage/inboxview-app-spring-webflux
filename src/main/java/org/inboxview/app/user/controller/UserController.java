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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;



@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public Mono<UserDto> getUser() {
        return userService.getUser();
    }

    @GetMapping("/mailbox-transaction/{year}/{month}")
    public Flux<UserMailboxTransaction> getMailboxTransaction(
        @PathVariable("month") Integer month,
        @PathVariable("year") Integer year
    ) {
        return userService.getMailboxTransactionByYearMonth(year, month);
    }

    @PutMapping("/{id}")
    public Mono<UserDto> update(
        @PathVariable String id,
        @RequestBody final UserDto userDto
    ) {
        return userService.updateUser(id, userDto);
    }    
}
