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
public class UserUpdateRequest {
    private String firstName;
    private String lastName;
    private String phone;
    private UserRole role;
    private Boolean active;
    private String password;
}

