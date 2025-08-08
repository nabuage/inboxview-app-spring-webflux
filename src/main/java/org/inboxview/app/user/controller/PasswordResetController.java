package org.inboxview.app.user.controller;

import org.inboxview.app.user.dto.PasswordResetRequestDto;
import org.inboxview.app.user.service.PasswordService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/password")
@RequiredArgsConstructor
public class PasswordResetController {
    private final PasswordService passwordService;

    @PostMapping("/email-reset")
    public Mono<ResponseEntity<Void>> emailReset(@RequestBody PasswordResetRequestDto request) {
        return passwordService
            .emailResetLink(request.username())
            .then(Mono.just(ResponseEntity.noContent().<Void>build()))
            .onErrorResume(e -> {
                return Mono.just(ResponseEntity.internalServerError().build());
            });
    }

    @PostMapping("/reset")
    public Mono<ResponseEntity<Void>> reset(@RequestBody PasswordResetRequestDto request) {
        return passwordService
            .reset(request)
            .then(Mono.just(ResponseEntity.noContent().<Void>build()))
            .onErrorResume(e -> {
                return Mono.just(ResponseEntity.internalServerError().build());
            });
    }
    
}
