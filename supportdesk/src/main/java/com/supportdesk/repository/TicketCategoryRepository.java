package com.supportdesk.repository;

import com.supportdesk.entity.TicketCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketCategoryRepository extends JpaRepository<TicketCategory, Long> {
    List<TicketCategory> findByOrganizationId(Long organizationId);
    List<TicketCategory> findByOrganizationIdAndActive(Long organizationId, Boolean active);
    List<TicketCategory> findByParentId(Long parentId);
    List<TicketCategory> findByOrganizationIdAndParentIsNull(Long organizationId);
}
