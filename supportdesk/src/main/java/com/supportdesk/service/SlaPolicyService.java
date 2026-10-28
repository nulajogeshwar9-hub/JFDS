package com.supportdesk.service;

import com.supportdesk.dto.SlaPolicyCreateRequest;
import com.supportdesk.dto.SlaPolicyDTO;
import com.supportdesk.entity.Organization;
import com.supportdesk.entity.SlaPolicy;
import com.supportdesk.exception.ResourceNotFoundException;
import com.supportdesk.repository.OrganizationRepository;
import com.supportdesk.repository.SlaPolicyRepository;
import com.supportdesk.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SlaPolicyService {

    private final SlaPolicyRepository slaPolicyRepository;
    private final OrganizationRepository organizationRepository;

    public SlaPolicyService(SlaPolicyRepository slaPolicyRepository,
                            OrganizationRepository organizationRepository) {
        this.slaPolicyRepository = slaPolicyRepository;
        this.organizationRepository = organizationRepository;
    }

    @Transactional(readOnly = true)
    public List<SlaPolicyDTO> getPoliciesByOrganization(Long orgId) {
        SecurityUtils.validateOrgAccess(orgId);
        return slaPolicyRepository.findByOrganizationId(orgId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SlaPolicyDTO getPolicyById(Long id) {
        SlaPolicy policy = slaPolicyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SLA Policy not found: " + id));
        SecurityUtils.validateOrgAccess(policy.getOrganization().getId());
        return mapToDTO(policy);
    }

    @Transactional
    public SlaPolicyDTO createPolicy(Long orgId, SlaPolicyCreateRequest request) {
        SecurityUtils.validateOrgAccess(orgId);
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found: " + orgId));

        SlaPolicy policy = SlaPolicy.builder()
                .name(request.getName())
                .priority(request.getPriority())
                .firstResponseMinutes(request.getFirstResponseMinutes())
                .resolutionMinutes(request.getResolutionMinutes())
                .organization(org)
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        policy = slaPolicyRepository.save(policy);
        return mapToDTO(policy);
    }

    @Transactional
    public SlaPolicyDTO updatePolicy(Long id, SlaPolicyCreateRequest request) {
        SlaPolicy policy = slaPolicyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SLA Policy not found: " + id));
        SecurityUtils.validateOrgAccess(policy.getOrganization().getId());

        if (request.getName() != null) policy.setName(request.getName());
        if (request.getPriority() != null) policy.setPriority(request.getPriority());
        if (request.getFirstResponseMinutes() != null) policy.setFirstResponseMinutes(request.getFirstResponseMinutes());
        if (request.getResolutionMinutes() != null) policy.setResolutionMinutes(request.getResolutionMinutes());
        if (request.getActive() != null) policy.setActive(request.getActive());

        policy = slaPolicyRepository.save(policy);
        return mapToDTO(policy);
    }

    public SlaPolicyDTO mapToDTO(SlaPolicy policy) {
        return SlaPolicyDTO.builder()
                .id(policy.getId())
                .name(policy.getName())
                .organizationId(policy.getOrganization() != null ? policy.getOrganization().getId() : null)
                .priority(policy.getPriority())
                .firstResponseMinutes(policy.getFirstResponseMinutes())
                .resolutionMinutes(policy.getResolutionMinutes())
                .active(policy.getActive())
                .build();
    }
}

