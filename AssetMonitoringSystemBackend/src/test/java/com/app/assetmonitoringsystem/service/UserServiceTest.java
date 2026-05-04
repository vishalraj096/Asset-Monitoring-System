package com.app.assetmonitoringsystem.service;

import com.app.assetmonitoringsystem.dto.AuthResponse;
import com.app.assetmonitoringsystem.dto.LoginRequest;
import com.app.assetmonitoringsystem.dto.RegisterRequest;
import com.app.assetmonitoringsystem.dto.UserDTO;
import com.app.assetmonitoringsystem.entity.Role;
import com.app.assetmonitoringsystem.entity.User;
import com.app.assetmonitoringsystem.exception.DuplicateResourceException;
import com.app.assetmonitoringsystem.exception.ResourceNotFoundException;
import com.app.assetmonitoringsystem.repository.UserRepository;
import com.app.assetmonitoringsystem.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtTokenProvider jwtTokenProvider;

    @InjectMocks private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("John Doe", "john@example.com", "encoded_password", Role.ROLE_OPERATOR);
        testUser.setId(1L);
    }

    @Test
    void register_Success() {
        RegisterRequest request = new RegisterRequest("John Doe", "john@example.com", "password123", "OPERATOR");
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtTokenProvider.generateToken("john@example.com")).thenReturn("jwt_token");

        AuthResponse response = userService.register(request);

        assertNotNull(response);
        assertEquals("jwt_token", response.getToken());
        assertEquals("john@example.com", response.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_DuplicateEmail_ThrowsException() {
        RegisterRequest request = new RegisterRequest("John Doe", "john@example.com", "password123", "OPERATOR");
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> userService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_Success() {
        LoginRequest request = new LoginRequest("john@example.com", "password123");
        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(jwtTokenProvider.generateToken(auth)).thenReturn("jwt_token");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));

        AuthResponse response = userService.login(request);

        assertNotNull(response);
        assertEquals("jwt_token", response.getToken());
    }

    @Test
    void getAllUsers_ReturnsList() {
        when(userRepository.findAll()).thenReturn(List.of(testUser));

        List<UserDTO> users = userService.getAllUsers();

        assertEquals(1, users.size());
        assertEquals("john@example.com", users.get(0).getEmail());
    }

    @Test
    void updateUserRole_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserDTO updated = userService.updateUserRole(1L, "MANAGER");

        assertNotNull(updated);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUserRole_UserNotFound_ThrowsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.updateUserRole(99L, "MANAGER"));
    }
}
