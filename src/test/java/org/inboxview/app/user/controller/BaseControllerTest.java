package org.inboxview.app.user.controller;

import org.inboxview.app.config.JwtConfig;
import org.inboxview.app.config.JwtService;
import org.inboxview.app.config.SecurityConfig;
import org.inboxview.app.user.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@Import({SecurityConfig.class, JwtConfig.class})
public class BaseControllerTest {
    @Autowired
    protected MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;
}
