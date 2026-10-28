package com.supportdesk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketTrendDTO {
    private String date;
    private long createdCount;
    private long resolvedCount;
}

