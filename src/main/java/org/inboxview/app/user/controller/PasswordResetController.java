package org.inboxview.app.user.controller;

import org.inboxview.app.user.dto.PasswordResetRequestDto;
import org.inboxview.app.user.service.PasswordService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/password")
@RequiredArgsConstructor
public class PasswordResetController {
    private final PasswordService passwordService;

    @PostMapping("/email-reset")
    public Mono<Void> emailReset(@RequestBody PasswordResetRequestDto request) {
        return passwordService.emailResetLink(request.username());
    }

    @PostMapping("/reset")
    public Mono<Void> reset(@RequestBody PasswordResetRequestDto request) {
        return passwordService.reset(request);
    }
    
}
