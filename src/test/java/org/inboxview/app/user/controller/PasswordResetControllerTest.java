package org.inboxview.app.user.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.inboxview.app.error.InvalidRequest;
import org.inboxview.app.user.dto.PasswordResetRequestDto;
import org.inboxview.app.user.service.PasswordService;
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
public class PasswordResetControllerTest extends BaseControllerTest {
    private static final String PASSWORD_NOT_EQUAL_ERROR = "Password and Password Confirmation must be the same.";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @MockitoBean
    private PasswordService passwordService;

    private PasswordResetRequestDto request;
    private String usernameRequest = "";

    @BeforeEach
    public void setup() {
        request = PasswordResetRequestDto.builder()
            .username("username")
            .password("password")
            .passwordConfirmation("password")
            .token("token")
            .build();
        
        usernameRequest = """
            {
                "username": "%s"
            }
            """.formatted(request.username());
    }

    @Test
    public void testEmailResetReturnsSuccess() throws Exception {
        when(passwordService.emailResetLink(request.username())).thenReturn(Mono.empty());

        MvcResult result = mockMvc.perform(
                post("/api/password/email-reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content(OBJECT_MAPPER.writeValueAsString(request))
            )
            .andExpect(status().isOk())
            .andExpect(request().asyncStarted())
            .andReturn();

        mockMvc.perform(asyncDispatch(result))
            .andExpect(status().isNoContent());

        verify(passwordService, times(1)).emailResetLink(anyString());
    }

    @Test
    public void testEmailResetReturnsInternalServerError() throws Exception {
        when(passwordService.emailResetLink(request.username())).thenThrow(new RuntimeException());

        mockMvc.perform(
                post("/api/password/email-reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content(OBJECT_MAPPER.writeValueAsString(request))
            )
            .andExpect(status().isInternalServerError())
            .andReturn();

        verify(passwordService, times(1)).emailResetLink(anyString());
    }

    @Test
    public void testResetReturnsSuccess() throws Exception {
        when(passwordService.reset(request)).thenReturn(Mono.empty());

        MvcResult result = mockMvc.perform(
                post("/api/password/reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content(OBJECT_MAPPER.writeValueAsString(request))
            )
            .andExpect(status().isOk())
            .andExpect(request().asyncStarted())
            .andReturn();

        mockMvc.perform(asyncDispatch(result))
            .andExpect(status().isNoContent());

        verify(passwordService, times(1)).reset(request);
    }

    @Test
    public void testResetReturnsPasswordNotEqualInvalidRequestException() throws Exception {
        doThrow(new InvalidRequest(PASSWORD_NOT_EQUAL_ERROR)).when(passwordService).reset(request);

        mockMvc.perform(
                post("/api/password/reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content(OBJECT_MAPPER.writeValueAsString(request))
            )
            .andExpect(status().isInternalServerError())
            .andExpect(MockMvcResultMatchers.jsonPath("$.error").value(PASSWORD_NOT_EQUAL_ERROR));

        verify(passwordService, times(1)).reset(request);
    }
}
