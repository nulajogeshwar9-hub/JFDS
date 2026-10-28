package com.supportdesk.repository;

import com.supportdesk.entity.Priority;
import com.supportdesk.entity.Ticket;
import com.supportdesk.entity.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByTicketNumber(String ticketNumber);

    Page<Ticket> findByOrganizationId(Long organizationId, Pageable pageable);

    Page<Ticket> findByCustomerId(Long customerId, Pageable pageable);

    Page<Ticket> findByAssignedAgentId(Long agentId, Pageable pageable);

    Page<Ticket> findByOrganizationIdAndStatus(Long organizationId, TicketStatus status, Pageable pageable);

    Page<Ticket> findByOrganizationIdAndPriority(Long organizationId, Priority priority, Pageable pageable);

    Page<Ticket> findByAssignedTeamId(Long teamId, Pageable pageable);

    // Dashboard statistics queries
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.organization.id = :orgId")
    long countByOrganizationId(@Param("orgId") Long orgId);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.organization.id = :orgId AND t.status = :status")
    long countByOrganizationIdAndStatus(@Param("orgId") Long orgId, @Param("status") TicketStatus status);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.assignedAgent.id = :agentId AND t.status = :status")
    long countByAssignedAgentIdAndStatus(@Param("agentId") Long agentId, @Param("status") TicketStatus status);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.organization.id = :orgId AND t.priority = :priority")
    long countByOrganizationIdAndPriority(@Param("orgId") Long orgId, @Param("priority") Priority priority);

    // SLA breach counts
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.organization.id = :orgId AND t.slaFirstResponseBreached = true")
    long countFirstResponseBreachesByOrganizationId(@Param("orgId") Long orgId);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.organization.id = :orgId AND t.slaResolutionBreached = true")
    long countResolutionBreachesByOrganizationId(@Param("orgId") Long orgId);

    // Tickets by date range
    @Query("SELECT t FROM Ticket t WHERE t.organization.id = :orgId AND t.createdAt BETWEEN :startDate AND :endDate")
    List<Ticket> findByOrganizationIdAndCreatedAtBetween(
            @Param("orgId") Long orgId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Unassigned tickets
    @Query("SELECT t FROM Ticket t WHERE t.organization.id = :orgId AND t.assignedAgent IS NULL AND t.status NOT IN ('RESOLVED', 'CLOSED')")
    List<Ticket> findUnassignedTickets(@Param("orgId") Long orgId);

    // Tickets with SLA deadline approaching
    @Query("SELECT t FROM Ticket t WHERE t.organization.id = :orgId AND t.firstResponseDeadline IS NOT NULL AND t.firstResponseAt IS NULL AND t.firstResponseDeadline < :deadline")
    List<Ticket> findTicketsApproachingFirstResponseDeadline(
            @Param("orgId") Long orgId,
            @Param("deadline") LocalDateTime deadline);

    @Query("SELECT t FROM Ticket t WHERE t.organization.id = :orgId AND t.resolutionDeadline IS NOT NULL AND t.resolvedAt IS NULL AND t.resolutionDeadline < :deadline")
    List<Ticket> findTicketsApproachingResolutionDeadline(
            @Param("orgId") Long orgId,
            @Param("deadline") LocalDateTime deadline);

    // Count for domain-based reporting
    @Query("SELECT t.domain, COUNT(t) FROM Ticket t WHERE t.organization.id = :orgId GROUP BY t.domain")
    List<Object[]> countByDomain(@Param("orgId") Long orgId);

    // Count for status-based reporting
    @Query("SELECT t.status, COUNT(t) FROM Ticket t WHERE t.organization.id = :orgId GROUP BY t.status")
    List<Object[]> countByStatus(@Param("orgId") Long orgId);

    // Count for priority-based reporting
    @Query("SELECT t.priority, COUNT(t) FROM Ticket t WHERE t.organization.id = :orgId GROUP BY t.priority")
    List<Object[]> countByPriorityGroup(@Param("orgId") Long orgId);

    // Trend data - tickets per day
    @Query("SELECT CAST(t.createdAt AS date), COUNT(t) FROM Ticket t WHERE t.organization.id = :orgId AND t.createdAt BETWEEN :startDate AND :endDate GROUP BY CAST(t.createdAt AS date) ORDER BY CAST(t.createdAt AS date)")
    List<Object[]> getTicketTrend(
            @Param("orgId") Long orgId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Average resolution time in hours
    @Query("SELECT AVG(TIMESTAMPDIFF(HOUR, t.createdAt, t.resolvedAt)) FROM Ticket t WHERE t.organization.id = :orgId AND t.resolvedAt IS NOT NULL")
    Double getAverageResolutionTimeHours(@Param("orgId") Long orgId);

    // Average first response time in hours
    @Query("SELECT AVG(TIMESTAMPDIFF(HOUR, t.createdAt, t.firstResponseAt)) FROM Ticket t WHERE t.organization.id = :orgId AND t.firstResponseAt IS NOT NULL")
    Double getAverageFirstResponseTimeHours(@Param("orgId") Long orgId);
}
