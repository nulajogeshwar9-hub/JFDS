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
public class ReplyCreateRequest {
    @NotBlank(message = "Reply message cannot be blank")
    private String message;

    @Builder.Default
    private Boolean internalNote = false;
}

