package com.supportdesk.repository;

import com.supportdesk.entity.SlaBreach;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SlaBreachRepository extends JpaRepository<SlaBreach, Long> {
    List<SlaBreach> findByTicketId(Long ticketId);

    @Query("SELECT COUNT(sb) FROM SlaBreach sb WHERE sb.ticket.organization.id = :orgId AND sb.breachedAt BETWEEN :startDate AND :endDate")
    long countBreachesByOrganizationAndDateRange(
            @Param("orgId") Long orgId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT sb.breachType, COUNT(sb) FROM SlaBreach sb WHERE sb.ticket.organization.id = :orgId GROUP BY sb.breachType")
    List<Object[]> countByBreachType(@Param("orgId") Long orgId);
}
