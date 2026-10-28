package com.supportdesk.dto;

import com.supportdesk.entity.Priority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentRuleDTO {
    private Long id;
    private String name;
    private Long organizationId;
    private String domain;
    private Long categoryId;
    private String categoryName;
    private Priority priority;
    private Long assignTeamId;
    private String assignTeamName;
    private Long assignAgentId;
    private String assignAgentName;
    private Integer ruleOrder;
    private Boolean active;
}

