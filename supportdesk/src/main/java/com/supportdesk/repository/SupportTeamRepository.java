package com.supportdesk.repository;

import com.supportdesk.entity.SupportTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportTeamRepository extends JpaRepository<SupportTeam, Long> {
    List<SupportTeam> findByOrganizationId(Long organizationId);
    List<SupportTeam> findByOrganizationIdAndActive(Long organizationId, Boolean active);
}
