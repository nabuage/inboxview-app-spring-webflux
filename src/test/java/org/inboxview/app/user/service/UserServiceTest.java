package org.inboxview.app.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.inboxview.app.config.IAuthentication;
import org.inboxview.app.error.NotFoundException;
import org.inboxview.app.user.dto.UserDto;
import org.inboxview.app.user.entity.User;
import org.inboxview.app.user.mapper.UserMapper;
import org.inboxview.app.user.repository.UserRepository;
import org.inboxview.app.user.repository.projection.UserMailboxTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @InjectMocks
    UserService userService;

    @Mock
    UserRepository userRepository;

    @Mock
    UserMapper userMapper;

    @Mock
    Authentication authentication;

    @Mock
    IAuthentication iAuthentication;

    User user;
    UserDto userDto;

    @BeforeEach
    public void setup() {        
        user = User.builder()
            .id(1L)
            .username("email@inboxview.com")
            .password("password")
            .email("email@inboxview.com")
            .firstName("firstname")
            .lastName("lastname")
            .build();

        userDto = UserDto.builder()
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .phone(user.getPhone())
            .isVerified(Boolean.FALSE)
            .build();
    }

    @Test
    public void testGetUserReturnsSuccess() {
        when(iAuthentication.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(user.getEmail());
        when(userRepository.findByUsername(anyString())).thenReturn(Mono.just(user));
        when(userMapper.toDto(user)).thenReturn(userDto);

        var result = userService.getUser();

        StepVerifier.create(result)
            .expectNextMatches(userDto -> {
                assertThat(userDto).isEqualTo(this.userDto);
                return Boolean.TRUE;
            })
            .verifyComplete();

        verify(userRepository, times(1)).findByUsername(anyString());
    }

    @Test
    public void testRegisterReturnsUsernameNotFoundException() {
        when(iAuthentication.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(user.getEmail());
        when(userRepository.findByUsername(anyString())).thenReturn(Mono.empty());

        var result = userService.getUser();

        StepVerifier.create(result)
            .expectError(NotFoundException.class)
            .verify();

        verify(userRepository, times(1)).findByUsername(anyString());
    }

    @Test
    public void testGetMailboxTransactionByMonthReturnsSuccess() {
        when(iAuthentication.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(user.getEmail());
        when(userRepository.getByUsernameAndMonth(anyString(), anyInt())).thenReturn(mockUserMailboxTransactionList());

        var result = userService.getMailboxTransactionByMonth(LocalDate.now().getMonthValue());

        StepVerifier.create(result)
            .expectNextMatches(mailboxTransaction -> {
                assertThat(mailboxTransaction.getTransactionId()).isEqualTo(1L);
                assertThat(mailboxTransaction.getMerchantName()).isEqualTo("Merchant 1");
                assertThat(mailboxTransaction.getTransactionDate()).isEqualTo(LocalDate.now());
                assertThat(mailboxTransaction.getAmount()).isEqualTo(BigDecimal.valueOf(1.10));
                return Boolean.TRUE;
            })
            .expectNextMatches(mailboxTransaction -> {
                assertThat(mailboxTransaction.getTransactionId()).isEqualTo(2L);
                assertThat(mailboxTransaction.getMerchantName()).isEqualTo("Merchant 2");
                assertThat(mailboxTransaction.getTransactionDate()).isEqualTo(LocalDate.now());
                assertThat(mailboxTransaction.getAmount()).isEqualTo(BigDecimal.valueOf(20.10));
                return Boolean.TRUE;
            })
            .verifyComplete();

        verify(userRepository, times(1)).getByUsernameAndMonth(anyString(), anyInt());
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
