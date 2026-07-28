package org.admin.npapplication.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");

        // 1. Dynamic role check (customize this email check or query your database)
        boolean isAdmin = email != null && email.endsWith("@nugespharmacy.com");
        String role = isAdmin ? "ADMIN" : "CUSTOMER";

        // 2. Generate token with the correct role
        String token = jwtTokenProvider.generateToken(email, role);

        // 3. Set secure HTTP-only cookie
        Cookie jwtCookie = new Cookie("jwt", token);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true); // Required for HTTPS and SameSite=None
        jwtCookie.setAttribute("SameSite", "None"); // Allows cross-origin cookies
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(24 * 60 * 60);
        response.addCookie(jwtCookie);

        // 4. Route to the appropriate Vercel frontend based on role
        String targetUrl = isAdmin
                ? "https://np-admin-one.vercel.app/home?token=" + token
                : "https://nugesphramacy.vercel.app/home?token=" + token;

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}