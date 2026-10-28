package com.supportdesk.service;

import com.supportdesk.dto.RegisterRequest;
import com.supportdesk.dto.UserDTO;
import com.supportdesk.entity.Organization;
import com.supportdesk.entity.User;
import com.supportdesk.entity.UserRole;
import com.supportdesk.exception.BadRequestException;
import com.supportdesk.repository.OrganizationRepository;
import com.supportdesk.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, organizationRepository, passwordEncoder);
    }

    @Test
    void testRegisterNewCustomerSuccessfully() {
        Organization org = Organization.builder().id(1L).name("Default Org").build();

        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(organizationRepository.findAll()).thenReturn(List.of(org));
        when(passwordEncoder.encode("Secret@123")).thenReturn("$2a$10$hashedPassword");

        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(101L);
            return u;
        });

        RegisterRequest req = RegisterRequest.builder()
                .firstName("Alice")
                .lastName("Smith")
                .email("alice@example.com")
                .password("Secret@123")
                .phone("+1-555-0123")
                .build();

        UserDTO result = authService.register(req);

        assertNotNull(result);
        assertEquals("alice@example.com", result.getEmail());
        assertEquals(UserRole.CUSTOMER, result.getRole());
        assertEquals("Alice Smith", result.getFullName());
    }

    @Test
    void testRegisterDuplicateEmailThrowsBadRequest() {
        when(userRepository.existsByEmail("bob@example.com")).thenReturn(true);

        RegisterRequest req = RegisterRequest.builder()
                .firstName("Bob")
                .lastName("Jones")
                .email("bob@example.com")
                .password("Password@123")
                .build();

        assertThrows(BadRequestException.class, () -> authService.register(req));
        verify(userRepository, never()).save(any());
    }
}

