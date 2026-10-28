package com.supportdesk.dto;

import com.supportdesk.entity.Priority;
import com.supportdesk.entity.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketDetailDTO {
    private Long id;
    private String ticketNumber;
    private Long organizationId;
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private String subject;
    private String description;
    private String domain;
    private Long categoryId;
    private String categoryName;
    private Priority priority;
    private TicketStatus status;
    private Long assignedTeamId;
    private String assignedTeamName;
    private Long assignedAgentId;
    private String assignedAgentName;
    private LocalDateTime firstResponseDeadline;
    private LocalDateTime resolutionDeadline;
    private LocalDateTime firstResponseAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime closedAt;
    private Boolean slaFirstResponseBreached;
    private Boolean slaResolutionBreached;
    private Long slaRemainingMinutes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ReplyDTO> replies;
    private List<TicketHistoryDTO> history;
    private ClassificationSuggestionDTO classificationSuggestion;
}

