package org.inboxview.app.user.service;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class MessageServiceTest {
    private static final String FROM = "george@inboxview.com";
    @InjectMocks
    MessageSenderService messageSenderService;

    @Mock
    JavaMailSender mailSender;

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(messageSenderService, "FROM", FROM);
    }

    @Test
    public void testSendEmail() {
        String email = "george@inboxview.com";
        String subject = "subject";
        String body = "body";
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(FROM);
        message.setTo(email);
        message.setSubject(subject);
        message.setText(body);

        doNothing().when(mailSender).send(message);

        var result = messageSenderService.sendEmail(email, subject, body);

        StepVerifier
            .create(result)
            .expectNext(Boolean.TRUE)
            .verifyComplete();

        verify(mailSender, times(1)).send(message);
    }

    
}
