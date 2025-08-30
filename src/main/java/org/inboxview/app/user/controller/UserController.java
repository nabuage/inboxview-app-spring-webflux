package org.inboxview.app.user.controller;

import org.inboxview.app.user.dto.UserDto;
import org.inboxview.app.user.repository.projection.UserMailboxTransaction;
import org.inboxview.app.user.service.UserService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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

    @PutMapping("/")
    public Mono<UserDto> update(
        @RequestBody final UserDto userDto
    ) {
        return userService.updateUser(userDto);
    }

    @DeleteMapping("/")
    public Mono<ResponseEntity<Void>> delete() {
        return userService.deleteUser()
        .then(Mono.just(ResponseEntity.noContent().<Void>build()))
            .onErrorResume(e -> {
                return Mono.just(ResponseEntity.internalServerError().build());
            });
    }
}
