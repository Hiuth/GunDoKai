package org.example.gundokai.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public class SecurityUtil {
    public static String getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Unauthorized");
        }

        System.out.println("Authentication class: " + authentication.getClass().getName());
        System.out.println("Principal: " + authentication.getPrincipal());

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            System.out.println("JWT Claims: " + jwt.getClaims());
            return jwt.getClaimAsString("id");
        }

        return authentication.getName();
    }

}
