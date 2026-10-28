package com.supportdesk.service;

import com.supportdesk.dto.TicketCategoryDTO;
import com.supportdesk.entity.Organization;
import com.supportdesk.entity.TicketCategory;
import com.supportdesk.exception.ResourceNotFoundException;
import com.supportdesk.repository.OrganizationRepository;
import com.supportdesk.repository.TicketCategoryRepository;
import com.supportdesk.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final TicketCategoryRepository categoryRepository;
    private final OrganizationRepository organizationRepository;

    public CategoryService(TicketCategoryRepository categoryRepository,
                           OrganizationRepository organizationRepository) {
        this.categoryRepository = categoryRepository;
        this.organizationRepository = organizationRepository;
    }

    @Transactional(readOnly = true)
    public List<TicketCategoryDTO> getCategoriesByOrganization(Long orgId) {
        SecurityUtils.validateOrgAccess(orgId);
        return categoryRepository.findByOrganizationId(orgId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TicketCategoryDTO getCategoryById(Long id) {
        TicketCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
        SecurityUtils.validateOrgAccess(category.getOrganization().getId());
        return mapToDTO(category);
    }

    @Transactional
    public TicketCategoryDTO createCategory(Long orgId, TicketCategoryDTO dto) {
        SecurityUtils.validateOrgAccess(orgId);
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found: " + orgId));

        TicketCategory parent = null;
        if (dto.getParentId() != null) {
            parent = categoryRepository.findById(dto.getParentId()).orElse(null);
        }

        TicketCategory category = TicketCategory.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .organization(org)
                .parent(parent)
                .active(dto.getActive() != null ? dto.getActive() : true)
                .build();

        category = categoryRepository.save(category);
        return mapToDTO(category);
    }

    @Transactional
    public TicketCategoryDTO updateCategory(Long id, TicketCategoryDTO dto) {
        TicketCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
        SecurityUtils.validateOrgAccess(category.getOrganization().getId());

        if (dto.getName() != null) category.setName(dto.getName());
        if (dto.getDescription() != null) category.setDescription(dto.getDescription());
        if (dto.getActive() != null) category.setActive(dto.getActive());
        if (dto.getParentId() != null) {
            category.setParent(categoryRepository.findById(dto.getParentId()).orElse(null));
        }

        category = categoryRepository.save(category);
        return mapToDTO(category);
    }

    public TicketCategoryDTO mapToDTO(TicketCategory category) {
        return TicketCategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .organizationId(category.getOrganization() != null ? category.getOrganization().getId() : null)
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .active(category.getActive())
                .build();
    }
}

