package com.supportdesk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportTeamDTO {
    private Long id;
    private String name;
    private String description;
    private Long organizationId;
    private Boolean active;
    private int memberCount;
    private List<UserDTO> members;
}

