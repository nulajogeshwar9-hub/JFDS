package com.supportdesk.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sla_breaches")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlaBreach {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sla_policy_id", nullable = false)
    private SlaPolicy slaPolicy;

    @Enumerated(EnumType.STRING)
    @Column(name = "breach_type", nullable = false)
    private BreachType breachType;

    @Column(nullable = false)
    private LocalDateTime deadline;

    @Column(name = "breached_at")
    private LocalDateTime breachedAt;

    @PrePersist
    protected void onCreate() {
        if (breachedAt == null) breachedAt = LocalDateTime.now();
    }
}
