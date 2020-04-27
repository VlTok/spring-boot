package com.tokmeninov.springboot1.models;

import org.springframework.boot.web.reactive.context.GenericReactiveWebApplicationContext;
import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    USER, ADMIN;

    @Override
    public String getAuthority() {
        return name();
    }
}
