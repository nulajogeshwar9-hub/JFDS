package com.supportdesk.dto;

import com.supportdesk.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileDTO {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private UserRole role;
    private Long organizationId;
    private String organizationName;
    private String phone;
}

