package com.supportdesk.repository;

import com.supportdesk.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    Optional<Organization> findByDomain(String domain);
    Optional<Organization> findByName(String name);
}
