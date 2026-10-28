package com.supportdesk.dto;

import com.supportdesk.entity.Priority;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlaPolicyCreateRequest {
    @NotBlank(message = "Policy name is required")
    private String name;

    @NotNull(message = "Priority is required")
    private Priority priority;

    @NotNull(message = "First response minutes is required")
    @Min(value = 1, message = "First response minutes must be at least 1")
    private Integer firstResponseMinutes;

    @NotNull(message = "Resolution minutes is required")
    @Min(value = 1, message = "Resolution minutes must be at least 1")
    private Integer resolutionMinutes;

    @Builder.Default
    private Boolean active = true;
}

