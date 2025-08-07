package org.inboxview.app.user.controller;

import org.inboxview.app.user.dto.RegistrationRequestDto;
import org.inboxview.app.user.dto.UserDto;
import org.inboxview.app.user.dto.VerifyResendRequestDto;
import org.inboxview.app.user.service.RegistrationService;
import org.inboxview.app.user.service.VerificationService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/registration")
@RequiredArgsConstructor
public class RegistrationController {   
    private final RegistrationService registrationService;
    private final VerificationService verificationService;    

    @PostMapping("/register")
    public Mono<UserDto> registerUser(
        @RequestBody final RegistrationRequestDto request
    ) {
        return registrationService.register(request);
    }
    @GetMapping("/email/verify")
    public Mono<UserDto> verifyEmail(
        @RequestParam String id,
        @RequestParam String code
    ) {        
        return verificationService.verifyEmail(id, code);
    }

    @PostMapping("/email/resend-verify")
    public Mono<Void> resendEmailVerify(@RequestBody VerifyResendRequestDto request) {
        return verificationService.resendEmailVerification(request.id());
    }
}
