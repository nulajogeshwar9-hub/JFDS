package com.supportdesk.repository;

import com.supportdesk.entity.AssignmentRule;
import com.supportdesk.entity.Priority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignmentRuleRepository extends JpaRepository<AssignmentRule, Long> {
    List<AssignmentRule> findByOrganizationIdAndActiveOrderByRuleOrderAsc(Long organizationId, Boolean active);
    List<AssignmentRule> findByOrganizationIdOrderByRuleOrderAsc(Long organizationId);

    @Query("SELECT r FROM AssignmentRule r WHERE r.organization.id = :orgId AND r.active = true " +
           "AND (r.domain IS NULL OR r.domain = :domain) " +
           "AND (r.category.id IS NULL OR r.category.id = :categoryId) " +
           "AND (r.priority IS NULL OR r.priority = :priority) " +
           "ORDER BY r.ruleOrder ASC")
    List<AssignmentRule> findMatchingRules(
            @Param("orgId") Long orgId,
            @Param("domain") String domain,
            @Param("categoryId") Long categoryId,
            @Param("priority") Priority priority);
}
