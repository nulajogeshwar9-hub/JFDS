package com.supportdesk.service;

import com.supportdesk.dto.RegisterRequest;
import com.supportdesk.dto.UserDTO;
import com.supportdesk.dto.UserProfileDTO;
import com.supportdesk.entity.Organization;
import com.supportdesk.entity.User;
import com.supportdesk.entity.UserRole;
import com.supportdesk.exception.BadRequestException;
import com.supportdesk.exception.ResourceNotFoundException;
import com.supportdesk.repository.OrganizationRepository;
import com.supportdesk.repository.UserRepository;
import com.supportdesk.security.SecurityUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       OrganizationRepository organizationRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserDTO register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered: " + request.getEmail());
        }

        Organization org;
        if (request.getOrganizationId() != null) {
            org = organizationRepository.findById(request.getOrganizationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
        } else {
            org = organizationRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> new BadRequestException("No default organization available. Contact administrator."));
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(UserRole.CUSTOMER)
                .organization(org)
                .active(true)
                .emailVerified(false)
                .build();

        user = userRepository.save(user);

        return mapToDTO(user);
    }

    @Transactional(readOnly = true)
    public UserProfileDTO getCurrentUserProfile() {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return UserProfileDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .role(user.getRole())
                .organizationId(user.getOrganization() != null ? user.getOrganization().getId() : null)
                .organizationName(user.getOrganization() != null ? user.getOrganization().getName() : "")
                .phone(user.getPhone())
                .build();
    }

    @Transactional
    public UserProfileDTO updateProfile(UserProfileDTO profileDTO) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (profileDTO.getFirstName() != null && !profileDTO.getFirstName().isBlank()) {
            user.setFirstName(profileDTO.getFirstName().trim());
        }
        if (profileDTO.getLastName() != null && !profileDTO.getLastName().isBlank()) {
            user.setLastName(profileDTO.getLastName().trim());
        }
        if (profileDTO.getPhone() != null) {
            user.setPhone(profileDTO.getPhone().trim());
        }

        user = userRepository.save(user);

        return UserProfileDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .role(user.getRole())
                .organizationId(user.getOrganization() != null ? user.getOrganization().getId() : null)
                .organizationName(user.getOrganization() != null ? user.getOrganization().getName() : "")
                .phone(user.getPhone())
                .build();
    }

    public UserDTO mapToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .role(user.getRole())
                .organizationId(user.getOrganization() != null ? user.getOrganization().getId() : null)
                .organizationName(user.getOrganization() != null ? user.getOrganization().getName() : null)
                .phone(user.getPhone())
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .build();
    }
}

