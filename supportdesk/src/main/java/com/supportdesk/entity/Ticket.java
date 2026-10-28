package com.supportdesk.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_number", nullable = false, unique = true, length = 20)
    private String ticketNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(length = 50)
    private String domain;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private TicketCategory category;

    @Enumerated(EnumType.STRING)
    @Column
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column
    private TicketStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_team_id")
    private SupportTeam assignedTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_agent_id")
    private User assignedAgent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sla_policy_id")
    private SlaPolicy slaPolicy;

    @Column(name = "first_response_deadline")
    private LocalDateTime firstResponseDeadline;

    @Column(name = "resolution_deadline")
    private LocalDateTime resolutionDeadline;

    @Column(name = "first_response_at")
    private LocalDateTime firstResponseAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "sla_first_response_breached")
    private Boolean slaFirstResponseBreached;

    @Column(name = "sla_resolution_breached")
    private Boolean slaResolutionBreached;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (priority == null) priority = Priority.MEDIUM;
        if (status == null) status = TicketStatus.OPEN;
        if (slaFirstResponseBreached == null) slaFirstResponseBreached = false;
        if (slaResolutionBreached == null) slaResolutionBreached = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
