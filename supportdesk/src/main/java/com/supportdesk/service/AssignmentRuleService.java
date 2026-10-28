package com.supportdesk.service;

import com.supportdesk.dto.AssignmentRuleCreateRequest;
import com.supportdesk.dto.AssignmentRuleDTO;
import com.supportdesk.entity.*;
import com.supportdesk.exception.ResourceNotFoundException;
import com.supportdesk.repository.*;
import com.supportdesk.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AssignmentRuleService {

    private final AssignmentRuleRepository ruleRepository;
    private final OrganizationRepository organizationRepository;
    private final TicketCategoryRepository categoryRepository;
    private final SupportTeamRepository teamRepository;
    private final UserRepository userRepository;

    public AssignmentRuleService(AssignmentRuleRepository ruleRepository,
                                 OrganizationRepository organizationRepository,
                                 TicketCategoryRepository categoryRepository,
                                 SupportTeamRepository teamRepository,
                                 UserRepository userRepository) {
        this.ruleRepository = ruleRepository;
        this.organizationRepository = organizationRepository;
        this.categoryRepository = categoryRepository;
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<AssignmentRuleDTO> getRulesByOrganization(Long orgId) {
        SecurityUtils.validateOrgAccess(orgId);
        return ruleRepository.findByOrganizationIdOrderByRuleOrderAsc(orgId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public AssignmentRuleDTO createRule(Long orgId, AssignmentRuleCreateRequest request) {
        SecurityUtils.validateOrgAccess(orgId);
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found: " + orgId));

        TicketCategory category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId()).orElse(null);
        }

        SupportTeam team = null;
        if (request.getAssignTeamId() != null) {
            team = teamRepository.findById(request.getAssignTeamId()).orElse(null);
        }

        User agent = null;
        if (request.getAssignAgentId() != null) {
            agent = userRepository.findById(request.getAssignAgentId()).orElse(null);
        }

        AssignmentRule rule = AssignmentRule.builder()
                .name(request.getName())
                .organization(org)
                .domain(request.getDomain())
                .category(category)
                .priority(request.getPriority())
                .assignTeam(team)
                .assignAgent(agent)
                .ruleOrder(request.getRuleOrder() != null ? request.getRuleOrder() : 0)
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        rule = ruleRepository.save(rule);
        return mapToDTO(rule);
    }

    @Transactional
    public AssignmentRuleDTO updateRule(Long id, AssignmentRuleCreateRequest request) {
        AssignmentRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment Rule not found: " + id));
        SecurityUtils.validateOrgAccess(rule.getOrganization().getId());

        if (request.getName() != null) rule.setName(request.getName());
        if (request.getDomain() != null) rule.setDomain(request.getDomain());
        if (request.getPriority() != null) rule.setPriority(request.getPriority());
        if (request.getRuleOrder() != null) rule.setRuleOrder(request.getRuleOrder());
        if (request.getActive() != null) rule.setActive(request.getActive());

        if (request.getCategoryId() != null) {
            rule.setCategory(categoryRepository.findById(request.getCategoryId()).orElse(null));
        }
        if (request.getAssignTeamId() != null) {
            rule.setAssignTeam(teamRepository.findById(request.getAssignTeamId()).orElse(null));
        }
        if (request.getAssignAgentId() != null) {
            rule.setAssignAgent(userRepository.findById(request.getAssignAgentId()).orElse(null));
        }

        rule = ruleRepository.save(rule);
        return mapToDTO(rule);
    }

    @Transactional
    public void deleteRule(Long id) {
        AssignmentRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rule not found: " + id));
        SecurityUtils.validateOrgAccess(rule.getOrganization().getId());
        ruleRepository.delete(rule);
    }

    public AssignmentRuleDTO mapToDTO(AssignmentRule rule) {
        return AssignmentRuleDTO.builder()
                .id(rule.getId())
                .name(rule.getName())
                .organizationId(rule.getOrganization() != null ? rule.getOrganization().getId() : null)
                .domain(rule.getDomain())
                .categoryId(rule.getCategory() != null ? rule.getCategory().getId() : null)
                .categoryName(rule.getCategory() != null ? rule.getCategory().getName() : null)
                .priority(rule.getPriority())
                .assignTeamId(rule.getAssignTeam() != null ? rule.getAssignTeam().getId() : null)
                .assignTeamName(rule.getAssignTeam() != null ? rule.getAssignTeam().getName() : null)
                .assignAgentId(rule.getAssignAgent() != null ? rule.getAssignAgent().getId() : null)
                .assignAgentName(rule.getAssignAgent() != null ? rule.getAssignAgent().getFullName() : null)
                .ruleOrder(rule.getRuleOrder())
                .active(rule.getActive())
                .build();
    }
}

