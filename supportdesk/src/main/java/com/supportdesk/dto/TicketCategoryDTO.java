package com.supportdesk.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketCategoryDTO {
    private Long id;
    @NotBlank(message = "Category name is required")
    private String name;
    private String description;
    private Long organizationId;
    private Long parentId;
    private Boolean active;
}

