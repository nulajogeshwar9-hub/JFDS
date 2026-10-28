package com.supportdesk.repository;

import com.supportdesk.entity.User;
import com.supportdesk.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByRole(UserRole role);
    List<User> findByOrganizationId(Long organizationId);
    List<User> findByOrganizationIdAndRole(Long organizationId, UserRole role);
    List<User> findByOrganizationIdAndActive(Long organizationId, Boolean active);
    long countByOrganizationIdAndRole(Long organizationId, UserRole role);
}
