package com.supportdesk.service;

import com.supportdesk.dto.SupportTeamDTO;
import com.supportdesk.dto.UserDTO;
import com.supportdesk.entity.Organization;
import com.supportdesk.entity.SupportTeam;
import com.supportdesk.entity.TeamMembership;
import com.supportdesk.entity.User;
import com.supportdesk.exception.BadRequestException;
import com.supportdesk.exception.ResourceNotFoundException;
import com.supportdesk.repository.OrganizationRepository;
import com.supportdesk.repository.SupportTeamRepository;
import com.supportdesk.repository.TeamMembershipRepository;
import com.supportdesk.repository.UserRepository;
import com.supportdesk.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TeamService {

    private final SupportTeamRepository teamRepository;
    private final TeamMembershipRepository membershipRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public TeamService(SupportTeamRepository teamRepository,
                       TeamMembershipRepository membershipRepository,
                       OrganizationRepository organizationRepository,
                       UserRepository userRepository,
                       UserService userService) {
        this.teamRepository = teamRepository;
        this.membershipRepository = membershipRepository;
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public List<SupportTeamDTO> getTeamsByOrganization(Long orgId) {
        SecurityUtils.validateOrgAccess(orgId);
        return teamRepository.findByOrganizationId(orgId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SupportTeamDTO getTeamById(Long id) {
        SupportTeam team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + id));
        SecurityUtils.validateOrgAccess(team.getOrganization().getId());
        return mapToDTO(team);
    }

    @Transactional
    public SupportTeamDTO createTeam(Long orgId, SupportTeamDTO dto) {
        SecurityUtils.validateOrgAccess(orgId);
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found: " + orgId));

        SupportTeam team = SupportTeam.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .organization(org)
                .active(dto.getActive() != null ? dto.getActive() : true)
                .build();

        team = teamRepository.save(team);
        return mapToDTO(team);
    }

    @Transactional
    public SupportTeamDTO updateTeam(Long id, SupportTeamDTO dto) {
        SupportTeam team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + id));
        SecurityUtils.validateOrgAccess(team.getOrganization().getId());

        if (dto.getName() != null) team.setName(dto.getName());
        if (dto.getDescription() != null) team.setDescription(dto.getDescription());
        if (dto.getActive() != null) team.setActive(dto.getActive());

        team = teamRepository.save(team);
        return mapToDTO(team);
    }

    @Transactional
    public void addMember(Long teamId, Long userId, boolean isLead) {
        SupportTeam team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + teamId));
        SecurityUtils.validateOrgAccess(team.getOrganization().getId());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        if (membershipRepository.findByTeamIdAndUserId(teamId, userId).isPresent()) {
            throw new BadRequestException("User is already a member of this team");
        }

        TeamMembership membership = TeamMembership.builder()
                .team(team)
                .user(user)
                .isTeamLead(isLead)
                .build();

        membershipRepository.save(membership);
    }

    @Transactional
    public void removeMember(Long teamId, Long userId) {
        SupportTeam team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + teamId));
        SecurityUtils.validateOrgAccess(team.getOrganization().getId());

        membershipRepository.deleteByTeamIdAndUserId(teamId, userId);
    }

    public SupportTeamDTO mapToDTO(SupportTeam team) {
        List<TeamMembership> memberships = membershipRepository.findByTeamId(team.getId());
        List<UserDTO> members = memberships.stream()
                .map(m -> userService.mapToDTO(m.getUser()))
                .collect(Collectors.toList());

        return SupportTeamDTO.builder()
                .id(team.getId())
                .name(team.getName())
                .description(team.getDescription())
                .organizationId(team.getOrganization() != null ? team.getOrganization().getId() : null)
                .active(team.getActive())
                .memberCount(memberships.size())
                .members(members)
                .build();
    }
}

