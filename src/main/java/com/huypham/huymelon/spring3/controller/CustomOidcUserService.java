package com.huypham.huymelon.spring3.controller;

import com.huypham.huymelon.spring3.entity.User;
import com.huypham.huymelon.spring3.service.UserService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
public class CustomOidcUserService extends OidcUserService {

    private final UserService userService;

    public CustomOidcUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);
        String email = oidcUser.getEmail();
        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException("Google account does not provide an email address.");
        }
        String name = oidcUser.getFullName() != null ? oidcUser.getFullName() : oidcUser.getAttribute("name");
        User user = userService.processOAuthUser(email, name);

        return new CustomOidcUser(user, oidcUser);
    }
}

