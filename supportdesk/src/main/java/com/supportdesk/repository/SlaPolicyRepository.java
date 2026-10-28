package com.supportdesk.repository;

import com.supportdesk.entity.Priority;
import com.supportdesk.entity.SlaPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SlaPolicyRepository extends JpaRepository<SlaPolicy, Long> {
    List<SlaPolicy> findByOrganizationId(Long organizationId);
    Optional<SlaPolicy> findByOrganizationIdAndPriority(Long organizationId, Priority priority);
    List<SlaPolicy> findByOrganizationIdAndActive(Long organizationId, Boolean active);
}
