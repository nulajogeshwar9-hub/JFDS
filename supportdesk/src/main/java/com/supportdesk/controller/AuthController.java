package com.supportdesk.controller;

import com.supportdesk.dto.RegisterRequest;
import com.supportdesk.dto.UserDTO;
import com.supportdesk.dto.UserProfileDTO;
import com.supportdesk.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@Valid @RequestBody RegisterRequest request) {
        UserDTO user = authService.register(request);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileDTO> getCurrentUser() {
        UserProfileDTO profile = authService.getCurrentUserProfile();
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileDTO> updateProfile(@Valid @RequestBody UserProfileDTO profileDTO) {
        UserProfileDTO updated = authService.updateProfile(profileDTO);
        return ResponseEntity.ok(updated);
    }
}

