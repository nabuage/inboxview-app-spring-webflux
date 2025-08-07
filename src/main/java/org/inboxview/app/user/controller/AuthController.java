package org.inboxview.app.user.controller;

import org.inboxview.app.user.dto.AuthenticationRequestDto;
import org.inboxview.app.user.dto.AuthenticationResponseDto;
import org.inboxview.app.user.dto.RefreshTokenRequestDto;
import org.inboxview.app.user.service.AuthenticationService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public Mono<AuthenticationResponseDto> authenticate(
        @RequestBody final AuthenticationRequestDto request
    ) {        
        return authenticationService.authenticate(request);
    }
    
    @PostMapping("/refresh-token")
    public Mono<AuthenticationResponseDto> refreshToken(
        @RequestBody RefreshTokenRequestDto request
    ) {        
        return authenticationService.refreshToken(request);
    }

    @PostMapping("/logout")
    public Mono<Void> revokeToken(
        @RequestBody RefreshTokenRequestDto request
    ) {
        return authenticationService.revokeRefreshToken(request.refreshToken());
    }
}
