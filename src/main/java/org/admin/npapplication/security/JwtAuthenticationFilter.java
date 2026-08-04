package org.admin.npapplication.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.admin.npapplication.service.AdminCheckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private AdminCheckService adminCheckService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String jwt = getJwtFromRequest(request);

            if (jwt != null) {
                try {
                    if (tokenProvider.validateToken(jwt)) {
                        setAuthenticationFromJwt(jwt, request);
                    } else {
                        setAuthenticationFromFirebase(jwt, request);
                    }
                } catch (Exception ex) {
                    setAuthenticationFromFirebase(jwt, request);
                }
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    private void setAuthenticationFromJwt(String jwt, HttpServletRequest request) {
        String email = tokenProvider.getEmailFromJWT(jwt);
        String role = tokenProvider.getRoleFromJWT(jwt);
        setAuthentication(email, role, request);
    }

    private void setAuthenticationFromFirebase(String jwt, HttpServletRequest request) throws Exception {
        FirebaseToken firebaseToken = FirebaseAuth.getInstance().verifyIdToken(jwt);
        String email = firebaseToken.getEmail();
        boolean isAdmin = email != null && adminCheckService.isAdmin(email);
        String role = isAdmin ? "ROLE_ADMIN" : "ROLE_USER";
        setAuthentication(email, role, request);
    }

    private void setAuthentication(String email, String role, HttpServletRequest request) {
        if (email == null || email.isBlank()) {
            return;
        }

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority(role))
                );

        authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }

        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        for (Cookie cookie : cookies) {
            if ("jwt".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}