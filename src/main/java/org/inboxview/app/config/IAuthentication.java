package org.inboxview.app.config;

import org.springframework.security.core.Authentication;

public interface IAuthentication {
    Authentication getAuthentication();    
}
