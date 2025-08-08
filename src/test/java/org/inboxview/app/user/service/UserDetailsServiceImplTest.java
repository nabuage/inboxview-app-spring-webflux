package org.inboxview.app.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.inboxview.app.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
public class UserDetailsServiceImplTest {
    @InjectMocks
    UserDetailsServiceImpl userDetailsService;

    @Mock
    UserRepository userRepository;

    UserDetails userDetails;
    org.inboxview.app.user.entity.User user;

    @BeforeEach
    public void setup() {
        userDetails = User.builder()
            .username("username")
            .password("password")
            .build();
        
        user = org.inboxview.app.user.entity.User .builder()
            .id(1L)
            .username("username")
            .password("password")
            .email("email@inboxview.com")
            .firstName("firstname")
            .lastName("lastname")
            .build();
    }

    @Test
    public void testLoadUserByUsernameReturnsUserDetails() {
        when(userRepository.findByUsername(anyString())).thenReturn(Mono.just(user));

        var result = userDetailsService.loadUserByUsername(user.getUsername());

        assertThat(result).isEqualTo(userDetails);

        verify(userRepository, times(1)).findByUsername(anyString());
    }

    @Test
    public void testRegisterReturnsUsernameNotFoundException() {
        when(userRepository.findByUsername(anyString())).thenReturn(Mono.empty());

        Exception result = assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername(user.getUsername());
        });

        assertThat(result.getMessage()).isEqualTo("User is not found.");

        verify(userRepository, times(1)).findByUsername(anyString());
    }
    
}
