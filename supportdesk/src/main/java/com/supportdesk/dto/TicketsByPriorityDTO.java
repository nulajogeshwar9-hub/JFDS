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
public class TicketsByPriorityDTO {
    private Priority priority;
    private long count;
}

