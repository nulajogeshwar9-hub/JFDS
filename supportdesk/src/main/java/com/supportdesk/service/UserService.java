package com.supportdesk.service;

import com.supportdesk.dto.PageDTO;
import com.supportdesk.dto.UserDTO;
import com.supportdesk.dto.UserUpdateRequest;
import com.supportdesk.entity.Organization;
import com.supportdesk.entity.User;
import com.supportdesk.entity.UserRole;
import com.supportdesk.exception.BadRequestException;
import com.supportdesk.exception.ResourceNotFoundException;
import com.supportdesk.repository.OrganizationRepository;
import com.supportdesk.repository.UserRepository;
import com.supportdesk.security.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       OrganizationRepository organizationRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        SecurityUtils.validateOrgAccess(user.getOrganization() != null ? user.getOrganization().getId() : null);
        return mapToDTO(user);
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getUsersByOrganization(Long orgId) {
        SecurityUtils.validateOrgAccess(orgId);
        return userRepository.findByOrganizationId(orgId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getUsersByOrganizationAndRole(Long orgId, UserRole role) {
        SecurityUtils.validateOrgAccess(orgId);
        return userRepository.findByOrganizationIdAndRole(orgId, role).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserDTO createUser(Long orgId, UserDTO userDTO, String password) {
        SecurityUtils.validateOrgAccess(orgId);

        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new BadRequestException("Email is already in use: " + userDTO.getEmail());
        }

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found: " + orgId));

        User user = User.builder()
                .email(userDTO.getEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(password != null ? password : "Password@123"))
                .firstName(userDTO.getFirstName())
                .lastName(userDTO.getLastName())
                .role(userDTO.getRole() != null ? userDTO.getRole() : UserRole.CUSTOMER)
                .phone(userDTO.getPhone())
                .organization(org)
                .active(userDTO.getActive() != null ? userDTO.getActive() : true)
                .emailVerified(true)
                .build();

        user = userRepository.save(user);
        return mapToDTO(user);
    }

    @Transactional
    public UserDTO updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));

        SecurityUtils.validateOrgAccess(user.getOrganization() != null ? user.getOrganization().getId() : null);

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName().trim());
        if (request.getLastName() != null) user.setLastName(request.getLastName().trim());
        if (request.getPhone() != null) user.setPhone(request.getPhone().trim());
        if (request.getRole() != null) user.setRole(request.getRole());
        if (request.getActive() != null) user.setActive(request.getActive());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        user = userRepository.save(user);
        return mapToDTO(user);
    }

    @Transactional
    public UserDTO toggleUserActive(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        SecurityUtils.validateOrgAccess(user.getOrganization() != null ? user.getOrganization().getId() : null);

        boolean current = Boolean.TRUE.equals(user.getActive());
        user.setActive(!current);
        user = userRepository.save(user);
        return mapToDTO(user);
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

