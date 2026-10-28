package com.supportdesk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlaComplianceDTO {
    private String period;
    private long totalTickets;
    private long withinSlaCount;
    private long breachedCount;
    private double complianceRate;
}

