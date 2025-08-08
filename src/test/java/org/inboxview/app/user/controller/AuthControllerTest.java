package org.inboxview.app.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.inboxview.app.user.dto.AuthenticationRequestDto;
import org.inboxview.app.user.dto.AuthenticationResponseDto;
import org.inboxview.app.user.dto.RefreshTokenRequestDto;
import org.inboxview.app.user.service.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest extends BaseControllerTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String INVALID_CREDENTIALS = "Invalid credentials.";
    @MockitoBean
    private AuthenticationService authenticationService;

    private AuthenticationRequestDto request;
    private String jsonRequest;
    private RefreshTokenRequestDto refreshTokenRequest;

    @BeforeEach
    public void setup() {
        request = AuthenticationRequestDto.builder()
            .username("username")
            .password("password")
            .build();

        jsonRequest = """
            {
                "username": "%s",
                "password": "%s"
            }
            """.formatted(
                request.username(),
                request.password()
            );

        refreshTokenRequest = RefreshTokenRequestDto.builder()
            .accessToken("access-token")
            .refreshToken("refresh-token")
            .build();
    }

    @Test
    public void testAuthenticateReturnsSuccess() throws Exception {
        AuthenticationResponseDto response = AuthenticationResponseDto.builder()
            .accessToken("token")
            .refreshToken("refreshtoken")
            .build();
        
        when(authenticationService.authenticate(any(AuthenticationRequestDto.class))).thenReturn(Mono.just(response));

        MvcResult result = mockMvc.perform(
                post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
            )
            .andExpect(status().isOk())
            .andExpect(request().asyncStarted())
            .andReturn();

        mockMvc.perform(asyncDispatch(result))
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.accessToken").value(response.accessToken()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.refreshToken").value(response.refreshToken()));

        verify(authenticationService, times(1)).authenticate(any());
    }

    @Test
    public void tesAuthenticateReturnsBadCredentialsException() throws Exception {
        String error = "Login failed.";
        when(authenticationService.authenticate(any())).thenThrow(new BadCredentialsException(error));

        mockMvc.perform(
                post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
            )
            .andExpect(status().isUnauthorized())
            .andExpect(MockMvcResultMatchers.jsonPath("$.error").value(error));

        verify(authenticationService, times(1)).authenticate(any());
    }

    @Test
    public void testRefreshTokenReturnsSuccess() throws Exception {
        var response = AuthenticationResponseDto.builder()
            .accessToken(refreshTokenRequest.accessToken())
            .refreshToken(refreshTokenRequest.refreshToken())
            .build();

        when(authenticationService.refreshToken(any())).thenReturn(Mono.just(response));

        mockMvc.perform(
                post("/api/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(OBJECT_MAPPER.writeValueAsString(refreshTokenRequest))
            )
            .andExpect(status().isOk());

        verify(authenticationService, times(1)).refreshToken(any());
    }

    @Test
    public void testRefreshTokenReturnsBadCredentialsException() throws Exception {
        doThrow(new BadCredentialsException(INVALID_CREDENTIALS)).when(authenticationService).refreshToken(any());

        mockMvc.perform(
                post("/api/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(OBJECT_MAPPER.writeValueAsString(refreshTokenRequest))
            )
            .andExpect(status().isUnauthorized())
            .andExpect(MockMvcResultMatchers.jsonPath("$.error").value(INVALID_CREDENTIALS));

        verify(authenticationService, times(1)).refreshToken(any());
    }

    @Test
    public void testRevokeTokenReturnsSuccess() throws Exception {
        when(authenticationService.revokeRefreshToken(anyString())).thenReturn(Mono.empty());

        MvcResult result = mockMvc.perform(
                post("/api/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(OBJECT_MAPPER.writeValueAsString(refreshTokenRequest))
            )
            .andExpect(status().isOk())
            .andExpect(request().asyncStarted())
            .andReturn();

        mockMvc.perform(asyncDispatch(result))
            .andExpect(status().isNoContent());

        verify(authenticationService, times(1)).revokeRefreshToken(anyString());
    }

    @Test
    public void testRevokeTokenReturnsInternalServerError() throws Exception {
        when(authenticationService.revokeRefreshToken(anyString())).thenThrow(new RuntimeException());

        mockMvc.perform(
                post("/api/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(OBJECT_MAPPER.writeValueAsString(refreshTokenRequest))
            )
            .andExpect(status().isInternalServerError())
            .andReturn();

        verify(authenticationService, times(1)).revokeRefreshToken(anyString());
    }
}
