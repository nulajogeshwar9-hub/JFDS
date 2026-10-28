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
public class SlaPolicyDTO {
    private Long id;
    private String name;
    private Long organizationId;
    private Priority priority;
    private Integer firstResponseMinutes;
    private Integer resolutionMinutes;
    private Boolean active;
}

