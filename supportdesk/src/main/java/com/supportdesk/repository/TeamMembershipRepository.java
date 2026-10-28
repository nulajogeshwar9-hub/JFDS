package com.supportdesk.repository;

import com.supportdesk.entity.TeamMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamMembershipRepository extends JpaRepository<TeamMembership, Long> {
    List<TeamMembership> findByTeamId(Long teamId);
    List<TeamMembership> findByUserId(Long userId);
    Optional<TeamMembership> findByTeamIdAndUserId(Long teamId, Long userId);
    boolean existsByTeamIdAndUserId(Long teamId, Long userId);
    void deleteByTeamIdAndUserId(Long teamId, Long userId);
}
