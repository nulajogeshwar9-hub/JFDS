package com.supportdesk.dto;

import com.supportdesk.entity.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketsByStatusDTO {
    private TicketStatus status;
    private long count;
}

