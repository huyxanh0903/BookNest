package com.huypham.huymelon.spring3.controller;

import com.huypham.huymelon.spring3.entity.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.List;

public class CustomOidcUser extends DefaultOidcUser implements UserDetails {

    private final User user;

    public CustomOidcUser(User user, OidcUser oidcUser) {
        super(
                List.of(new SimpleGrantedAuthority("ROLE_" + (user.getRole() == null || user.getRole().isBlank()
                        ? "USER"
                        : (user.getRole().toUpperCase().startsWith("ROLE_") ? user.getRole().substring(5) : user.getRole()).toUpperCase()))),
                oidcUser.getIdToken(),
                oidcUser.getUserInfo(),
                "email"
        );
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(user.getEnabled());
    }
}

