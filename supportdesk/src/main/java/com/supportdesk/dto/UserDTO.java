package com.supportdesk.dto;

import com.supportdesk.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private UserRole role;
    private Long organizationId;
    private String organizationName;
    private String phone;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
}

