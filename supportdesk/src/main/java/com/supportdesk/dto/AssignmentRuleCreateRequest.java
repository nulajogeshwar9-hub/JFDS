package com.supportdesk.dto;

import com.supportdesk.entity.Priority;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentRuleCreateRequest {
    @NotBlank(message = "Rule name is required")
    private String name;
    private String domain;
    private Long categoryId;
    private Priority priority;
    private Long assignTeamId;
    private Long assignAgentId;
    private Integer ruleOrder;
    @Builder.Default
    private Boolean active = true;
}

