package com.servicedesk.config;

import com.servicedesk.model.AppUser;
import com.servicedesk.service.UserService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.List;

/** Hooks into the Google login: saves the user in our DB and maps them to ROLE_USER / ROLE_ADMIN. */
@Service
public class CustomOidcUserService extends OidcUserService {
    private final UserService userService;

    public CustomOidcUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest request) {
        OidcUser oidc = super.loadUser(request);
        AppUser u = userService.upsert(oidc.getEmail(), oidc.getFullName(), oidc.getPicture());
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole().name()));
        return new DefaultOidcUser(authorities, oidc.getIdToken(), oidc.getUserInfo());
    }
}
