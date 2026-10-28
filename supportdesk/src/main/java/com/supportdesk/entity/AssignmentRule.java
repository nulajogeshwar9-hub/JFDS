package com.supportdesk.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "assignment_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(length = 50)
    private String domain;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private TicketCategory category;

    @Enumerated(EnumType.STRING)
    @Column
    private Priority priority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assign_team_id")
    private SupportTeam assignTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assign_agent_id")
    private User assignAgent;

    @Column(name = "rule_order")
    private Integer ruleOrder;

    @Column
    private Boolean active;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (ruleOrder == null) ruleOrder = 0;
        if (active == null) active = true;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
