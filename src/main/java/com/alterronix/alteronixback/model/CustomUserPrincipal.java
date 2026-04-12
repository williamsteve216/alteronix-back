package com.alterronix.alteronixback.model;

import com.alterronix.alteronixback.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CustomUserPrincipal implements OidcUser {

    private User user;
    private Map<String, Object> attributes;

    public CustomUserPrincipal(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }

    public User getUser() {
        return user;
    }

    @Override
    public Map<String, Object> getClaims() {
        return attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getName() {
        return user.getEmail();
    }

    @Override
    public OidcIdToken getIdToken() {
        return null; // simplifié
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return null;
    }
}
