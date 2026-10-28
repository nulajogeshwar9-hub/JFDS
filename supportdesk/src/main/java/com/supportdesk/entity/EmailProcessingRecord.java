package com.supportdesk.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "email_processing_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailProcessingRecord {

    public enum EmailStatus {
        PROCESSED, FAILED, DUPLICATE, IGNORED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_id", unique = true)
    private String messageId;

    @Column(name = "from_address", nullable = false)
    private String fromAddress;

    @Column
    private String subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmailStatus status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @PrePersist
    protected void onCreate() {
        if (processedAt == null) processedAt = LocalDateTime.now();
    }
}
