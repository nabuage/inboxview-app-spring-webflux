package org.inboxview.app.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.inboxview.app.error.NotFoundException;
import org.inboxview.app.user.dto.UserDto;
import org.inboxview.app.user.entity.User;
import org.inboxview.app.user.mapper.UserMapper;
import org.inboxview.app.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

    User user;
    UserDto userDto;

    @BeforeEach
    public void setup() {        
        user = User.builder()
            .id(1L)
            .username("username")
            .password("password")
            .email("email@inboxview.com")
            .firstName("firstname")
            .lastName("lastname")
            .build();

        userDto = UserDto.builder()
            .email(user.getEmail())
            .username(user.getUsername())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .phone(user.getPhone())
            .isVerified(Boolean.FALSE)
            .build();
    }

    @Test
    public void testGetByUsernameReturnsSuccess() {
        when(userRepository.findByUsername(anyString())).thenReturn(Mono.just(user));
        when(userMapper.toDto(user)).thenReturn(userDto);

        var result = userService.getByUsername(user.getUsername());

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
        when(userRepository.findByUsername(anyString())).thenReturn(Mono.empty());

        var result = userService.getByUsername(user.getUsername());

        StepVerifier.create(result)
            .expectError(NotFoundException.class)
            .verify();

        verify(userRepository, times(1)).findByUsername(anyString());
    }
}
