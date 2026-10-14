package com.servicedesk.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/** Resolves the caller's email whether they logged in with Google or username/password. */
public final class AuthUtil {
    private AuthUtil() {}

    public static String email(Authentication auth) {
        Object p = auth.getPrincipal();
        String email = (p instanceof OidcUser o) ? o.getEmail() : auth.getName();
        return email.toLowerCase();
    }
}
