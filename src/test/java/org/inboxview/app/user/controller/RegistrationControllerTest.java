package org.inboxview.app.user.controller;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

import org.inboxview.app.error.DuplicateException;
import org.inboxview.app.error.NotFoundException;
import org.inboxview.app.user.dto.RegistrationRequestDto;
import org.inboxview.app.user.dto.UserDto;
import org.inboxview.app.user.dto.VerifyResendRequestDto;
import org.inboxview.app.user.service.RegistrationService;
import org.inboxview.app.user.service.VerificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;

@SpringBootTest
@AutoConfigureMockMvc
public class RegistrationControllerTest extends BaseControllerTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String USER_EXIST_ERROR = "Username already exists.";
    private static final String USER_NOT_FOUND_ERROR = "User is not found.";
    private static final String ALREADY_VERIFIED_ERROR = "Email already verified.";
    private final String PASSWORD = "password";

    @MockitoBean
    private RegistrationService registrationService;

    @MockitoBean
    private VerificationService verificationService;

    private UserDto user;
    private String jsonRequest;

    @BeforeEach
    public void setup() {
        user = UserDto.builder()
            .username("username")
            .email("email@inboxview.com")
            .firstName("firstname")
            .lastName("lastname")
            .build();

        jsonRequest = """
                {
                    "username": "%s",
                    "password": "%s",
                    "email": "%s",
                    "firstName": "%s",
                    "lastName": "%s"
                }
            """.formatted(
                user.username(),
                PASSWORD,
                user.email(),
                user.firstName(),
                user.lastName()
            );
    }

    @Test
    public void testRegisterSuccess() throws Exception {
        when(registrationService.register(any(RegistrationRequestDto.class))).thenReturn(Mono.just(user));

        MvcResult result = mockMvc.perform(
                post("/api/registration/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
            )
            .andExpect(status().isOk())
            .andExpect(request().asyncStarted())
            .andReturn();

        mockMvc.perform(asyncDispatch(result))
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.username").value(user.username()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.email").value(user.email()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value(user.firstName()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value(user.lastName()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.phone").value(user.phone()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.isVerified").value(Boolean.FALSE));

        verify(registrationService, times(1)).register(any());
    }

    @Test
    public void testRegisterReturnsDuplicateException() throws Exception {
        when(registrationService.register(any())).thenThrow(new DuplicateException(USER_EXIST_ERROR));
        
        mockMvc.perform(
                post("/api/registration/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
            )
            .andExpect(status().is4xxClientError())
            .andExpect(MockMvcResultMatchers.jsonPath("$.error").value(USER_EXIST_ERROR));

        verify(registrationService, times(1)).register(any());
    }

    @Test
    public void testVerifyEmailReturnsSuccess() throws Exception {
        var id = "guid";
        var code = "code";

        when(verificationService.verifyEmail(anyString(), anyString())).thenReturn(Mono.just(user));

        mockMvc.perform(
                get("/api/registration/email/verify?id=%s&code=%s".formatted(id, code))
            )
            .andExpect(status().isOk());

        verify(verificationService, times(1)).verifyEmail(anyString(), anyString());
    }

    @Test
    public void testVerifyEmailReturnsNotFoundException() throws Exception {
        var id = "guid";
        var code = "code";

        doThrow(new NotFoundException(USER_NOT_FOUND_ERROR)).when(verificationService).verifyEmail(anyString(), anyString());

        mockMvc.perform(
                get("/api/registration/email/verify?id=%s&code=%s".formatted(id, code))
            )
            .andExpect(status().isNotFound())
            .andExpect(MockMvcResultMatchers.jsonPath("$.error").value(USER_NOT_FOUND_ERROR));

        verify(verificationService, times(1)).verifyEmail(anyString(), anyString());
    }

    @Test
    public void testResendVerifyReturnsSuccess() throws Exception {
        var id = "guid";
        var request = new VerifyResendRequestDto(id);

        when(verificationService.resendEmailVerification(anyString())).thenReturn(Mono.empty());

        MvcResult result = mockMvc.perform(
                post("/api/registration/email/resend-verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(OBJECT_MAPPER.writeValueAsString(request))
            )
            .andExpect(status().isOk())
            .andExpect(request().asyncStarted())
            .andReturn();

        mockMvc.perform(asyncDispatch(result))
            .andExpect(status().isNoContent());

        verify(verificationService, times(1)).resendEmailVerification(anyString());
    }

    @Test
    public void testResendVerifyReturnsInternalServerError() throws Exception {
        var id = "guid";
        var request = new VerifyResendRequestDto(id);

        when(verificationService.resendEmailVerification(anyString())).thenThrow(new RuntimeException());

        mockMvc.perform(
                post("/api/registration/email/resend-verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(OBJECT_MAPPER.writeValueAsString(request))
            )
            .andExpect(status().isInternalServerError())
            .andReturn();

        verify(verificationService, times(1)).resendEmailVerification(anyString());
    }

    @Test
    public void testResendVerifyReturnsNotFoundException() throws Exception {
        var id = "guid";
        var request = new VerifyResendRequestDto(id);

        doThrow(new NotFoundException(USER_NOT_FOUND_ERROR)).when(verificationService).resendEmailVerification(anyString());

        mockMvc.perform(
                post("/api/registration/email/resend-verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(OBJECT_MAPPER.writeValueAsString(request))
            )
            .andExpect(status().isNotFound())
            .andExpect(MockMvcResultMatchers.jsonPath("$.error").value(USER_NOT_FOUND_ERROR));

        verify(verificationService, times(1)).resendEmailVerification(anyString());
    }

    @Test
    public void testResendVerifyReturnsDuplicationException() throws Exception {
        var id = "guid";
        var request = new VerifyResendRequestDto(id);

        doThrow(new DuplicateException(ALREADY_VERIFIED_ERROR)).when(verificationService).resendEmailVerification(anyString());

        mockMvc.perform(
                post("/api/registration/email/resend-verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(OBJECT_MAPPER.writeValueAsString(request))
            )
            .andExpect(status().is4xxClientError())
            .andExpect(MockMvcResultMatchers.jsonPath("$.error").value(ALREADY_VERIFIED_ERROR));

        verify(verificationService, times(1)).resendEmailVerification(anyString());
    }
}
