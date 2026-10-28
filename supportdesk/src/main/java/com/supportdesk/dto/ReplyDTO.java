package com.supportdesk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReplyDTO {
    private Long id;
    private Long ticketId;
    private Long userId;
    private String userName;
    private String userRole;
    private String message;
    private Boolean internalNote;
    private LocalDateTime createdAt;
}

