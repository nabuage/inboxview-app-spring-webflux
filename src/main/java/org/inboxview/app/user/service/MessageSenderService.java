package org.inboxview.app.user.service;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class MessageSenderService {
    private final JavaMailSender mailSender;

    @Value("${app.from-email}")
    private String FROM;

    public Mono<Boolean> sendEmail(String email, String subject, String body) {
        CompletableFuture.runAsync(() -> {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setFrom(FROM);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
        });
        return Mono.just(Boolean.TRUE);
    }
}
