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

        // Dynamic role check
        boolean isAdmin = email != null && email.endsWith("@nugespharmacy.com");
        String role = isAdmin ? "ADMIN" : "CUSTOMER";

        String token = jwtTokenProvider.generateToken(email, role);

        Cookie jwtCookie = new Cookie("jwt", token);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true);
        jwtCookie.setAttribute("SameSite", "None");
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(24 * 60 * 60);
        response.addCookie(jwtCookie);

        String targetUrl = isAdmin
                ? "https://np-admin-one.vercel.app/home?token=" + token
                : "https://nugespharmacy.vercel.app/home?token=" + token;

        // 👉 Force clear any saved request cache so it doesn't fallback to localhost
        clearAuthenticationAttributes(request);
        
        // 👉 Directly redirect to the target URL
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}