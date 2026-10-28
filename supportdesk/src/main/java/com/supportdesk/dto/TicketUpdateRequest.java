package com.supportdesk.dto;

import com.supportdesk.entity.Priority;
import com.supportdesk.entity.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketUpdateRequest {
    private TicketStatus status;
    private Priority priority;
    private Long assignedTeamId;
    private Long assignedAgentId;
    private Long categoryId;
    private String domain;
    private String comment;
}

