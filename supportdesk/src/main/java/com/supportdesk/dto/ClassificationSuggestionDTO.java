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
public class ClassificationSuggestionDTO {
    private Long suggestedCategoryId;
    private String suggestedCategoryName;
    private Priority suggestedPriority;
    private Long suggestedTeamId;
    private String suggestedTeamName;
    private double confidenceScore;
    private String reasoning;
}

