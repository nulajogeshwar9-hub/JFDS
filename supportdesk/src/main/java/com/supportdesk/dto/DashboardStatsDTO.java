package com.supportdesk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsDTO {
    private long totalTickets;
    private long openTickets;
    private long inProgressTickets;
    private long waitingTickets;
    private long resolvedTickets;
    private long closedTickets;
    private long overdueTickets;
    private double avgFirstResponseMinutes;
    private double avgResolutionMinutes;
    private double slaComplianceRate;
}

