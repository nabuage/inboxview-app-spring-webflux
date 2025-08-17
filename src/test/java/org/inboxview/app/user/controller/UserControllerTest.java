package org.inboxview.app.user.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.inboxview.app.user.dto.UserDto;
import org.inboxview.app.user.repository.projection.UserMailboxTransaction;
import org.inboxview.app.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest extends BaseControllerTest {
    @MockitoBean
    UserService userService;

    private static final String USERNAME = "username";
    private UserDto user;

    @BeforeEach
    public void setup() {
        user = UserDto.builder()
            .email("email@inboxview.com")
            .firstName("firstname")
            .lastName("lastname")
            .build();
    }

    @Test
    @WithMockUser(username = USERNAME)
    public void testGetByUserReturnsSuccess() throws Exception {
        when(userService.getUser()).thenReturn(Mono.just(user));

        MvcResult result = mockMvc.perform(
                get("/api/user/me")            
            )
            .andExpect(status().isOk())
            .andExpect(request().asyncStarted())
            .andReturn();
        
        mockMvc.perform(asyncDispatch(result))
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.email").value(user.email()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value(user.firstName()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value(user.lastName()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.phone").value(user.phone()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.isVerified").value(Boolean.FALSE));
        
        verify(userService, times(1)).getUser();
    }

    @Test
    public void testGetUserReturnsUnauthorized() throws Exception {
        mockMvc.perform(
            get("/api/user/me")            
        )
        .andExpect(status().isUnauthorized());
        
        verify(userService, times(0)).getUser();
    }

    @Test
    @WithMockUser(username = USERNAME)
    public void testGetMailboxTransactionReturnsSuccess() throws Exception {
        when(userService.getMailboxTransactionByMonth(anyInt())).thenReturn(mockUserMailboxTransactionList());

        MvcResult result = mockMvc.perform(
                get("/api/user/mailbox-transaction/" + LocalDate.now().getMonthValue())            
            )
            .andExpect(status().isOk())
            .andExpect(request().asyncStarted())
            .andReturn();
        
        mockMvc.perform(asyncDispatch(result))
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty())
            .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
            .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(2))
            .andExpect(MockMvcResultMatchers.jsonPath("$.[0].transactionId").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.[0].merchantName").value("Merchant 1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.[0].transactionDate").value(LocalDate.now().toString()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.[0].amount").value("1.1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.[1].transactionId").value(2))
            .andExpect(MockMvcResultMatchers.jsonPath("$.[1].merchantName").value("Merchant 2"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.[1].transactionDate").value(LocalDate.now().toString()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.[1].amount").value("20.1"));
        
        verify(userService, times(1)).getMailboxTransactionByMonth(anyInt());
    }

    @Test
    public void testGetMailboxTransactionReturnsUnauthorized() throws Exception {
        mockMvc.perform(
            get("/api/user/mailbox-transaction/" + LocalDate.now().getMonthValue())            
        )
        .andExpect(status().isUnauthorized());
        
        verify(userService, times(0)).getMailboxTransactionByMonth(anyInt());
    }

    private Flux<UserMailboxTransaction> mockUserMailboxTransactionList() {
        return Flux.just(
            UserMailboxTransaction.builder()
                .transactionId(1L)
                .merchantName("Merchant 1")
                .transactionDate(LocalDate.now())
                .amount(BigDecimal.valueOf(1.10))
                .build(),
            UserMailboxTransaction.builder()
                .transactionId(2L)
                .merchantName("Merchant 2")
                .transactionDate(LocalDate.now())
                .amount(BigDecimal.valueOf(20.10))
                .build()
        );
    }
}
