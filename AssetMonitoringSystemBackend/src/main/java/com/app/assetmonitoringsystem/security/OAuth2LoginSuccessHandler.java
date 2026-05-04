package com.app.assetmonitoringsystem.security;

import com.app.assetmonitoringsystem.entity.Role;
import com.app.assetmonitoringsystem.entity.User;
import com.app.assetmonitoringsystem.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

/**
 * Handles successful OAuth2 login by creating or updating the user in the database
 * and issuing a JWT token in the response.
 */
@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2LoginSuccessHandler.class);

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public OAuth2LoginSuccessHandler(UserRepository userRepository,
                                     JwtTokenProvider jwtTokenProvider,
                                     PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        if (email == null) {
            // GitHub may not provide email; use login as fallback
            String login = oAuth2User.getAttribute("login");
            email = login + "@github.oauth";
            if (name == null) {
                name = login;
            }
        }

        Optional<User> existingUser = userRepository.findByEmail(email);
        User user;

        if (existingUser.isPresent()) {
            user = existingUser.get();
            logger.info("OAuth2 login: Existing user found - {}", email);
        } else {
            user = new User();
            user.setName(name != null ? name : "OAuth User");
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            user.setRole(Role.ROLE_OPERATOR);
            user = userRepository.save(user);
            logger.info("OAuth2 login: New user created - {}", email);
        }

        String token = jwtTokenProvider.generateToken(user.getEmail());

        // Return JWT token as JSON response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(String.format(
                "{\"token\":\"%s\",\"email\":\"%s\",\"role\":\"%s\",\"message\":\"OAuth2 login successful\"}",
                token, user.getEmail(), user.getRole().name()));
    }
}
