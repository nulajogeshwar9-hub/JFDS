package com.supportdesk.controller;

import com.supportdesk.dto.*;
import com.supportdesk.entity.UserRole;
import com.supportdesk.security.SecurityUtils;
import com.supportdesk.service.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final TicketService ticketService;
    private final UserService userService;
    private final TeamService teamService;
    private final CategoryService categoryService;
    private final SlaPolicyService slaPolicyService;
    private final AssignmentRuleService assignmentRuleService;
    private final DashboardService dashboardService;
    private final ReportService reportService;

    public AdminController(TicketService ticketService,
                           UserService userService,
                           TeamService teamService,
                           CategoryService categoryService,
                           SlaPolicyService slaPolicyService,
                           AssignmentRuleService assignmentRuleService,
                           DashboardService dashboardService,
                           ReportService reportService) {
        this.ticketService = ticketService;
        this.userService = userService;
        this.teamService = teamService;
        this.categoryService = categoryService;
        this.slaPolicyService = slaPolicyService;
        this.assignmentRuleService = assignmentRuleService;
        this.dashboardService = dashboardService;
        this.reportService = reportService;
    }

    // Ticket Assignment & Updating
    @PutMapping("/tickets/{id}/assign")
    public ResponseEntity<TicketDTO> assignTicket(@PathVariable Long id,
                                                  @RequestBody Map<String, Long> payload) {
        Long agentId = payload.get("agentId");
        Long teamId = payload.get("teamId");
        Long actorId = SecurityUtils.getCurrentUserId();
        TicketDTO updated = ticketService.assignTicket(id, agentId, teamId, actorId);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/tickets/{id}")
    public ResponseEntity<TicketDTO> updateTicket(@PathVariable Long id,
                                                  @RequestBody TicketUpdateRequest request) {
        Long actorId = SecurityUtils.getCurrentUserId();
        TicketDTO updated = ticketService.updateTicketDetails(id, request, actorId);
        return ResponseEntity.ok(updated);
    }

    // User Management
    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> listUsers(@RequestParam(required = false) UserRole role) {
        Long orgId = SecurityUtils.getCurrentOrgId();
        List<UserDTO> users = role != null
                ? userService.getUsersByOrganizationAndRole(orgId, role)
                : userService.getUsersByOrganization(orgId);
        return ResponseEntity.ok(users);
    }

    @PostMapping("/users")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserDTO userDTO,
                                              @RequestParam(required = false) String password) {
        Long orgId = SecurityUtils.getCurrentOrgId();
        UserDTO created = userService.createUser(orgId, userDTO, password);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id,
                                              @RequestBody UserUpdateRequest request) {
        UserDTO updated = userService.updateUser(id, request);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/users/{id}/toggle-active")
    public ResponseEntity<UserDTO> toggleUserActive(@PathVariable Long id) {
        UserDTO updated = userService.toggleUserActive(id);
        return ResponseEntity.ok(updated);
    }

    // Support Teams Management
    @GetMapping("/teams")
    public ResponseEntity<List<SupportTeamDTO>> listTeams() {
        Long orgId = SecurityUtils.getCurrentOrgId();
        return ResponseEntity.ok(teamService.getTeamsByOrganization(orgId));
    }

    @PostMapping("/teams")
    public ResponseEntity<SupportTeamDTO> createTeam(@Valid @RequestBody SupportTeamDTO dto) {
        Long orgId = SecurityUtils.getCurrentOrgId();
        return new ResponseEntity<>(teamService.createTeam(orgId, dto), HttpStatus.CREATED);
    }

    @PutMapping("/teams/{id}")
    public ResponseEntity<SupportTeamDTO> updateTeam(@PathVariable Long id,
                                                     @RequestBody SupportTeamDTO dto) {
        return ResponseEntity.ok(teamService.updateTeam(id, dto));
    }

    @PostMapping("/teams/{id}/members")
    public ResponseEntity<Void> addTeamMember(@PathVariable Long id,
                                              @RequestBody Map<String, Object> payload) {
        Long userId = Long.valueOf(payload.get("userId").toString());
        boolean isLead = Boolean.parseBoolean(String.valueOf(payload.getOrDefault("isLead", "false")));
        teamService.addMember(id, userId, isLead);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/teams/{id}/members/{userId}")
    public ResponseEntity<Void> removeTeamMember(@PathVariable Long id, @PathVariable Long userId) {
        teamService.removeMember(id, userId);
        return ResponseEntity.noContent().build();
    }

    // Categories Management
    @GetMapping("/categories")
    public ResponseEntity<List<TicketCategoryDTO>> listCategories() {
        Long orgId = SecurityUtils.getCurrentOrgId();
        return ResponseEntity.ok(categoryService.getCategoriesByOrganization(orgId));
    }

    @PostMapping("/categories")
    public ResponseEntity<TicketCategoryDTO> createCategory(@Valid @RequestBody TicketCategoryDTO dto) {
        Long orgId = SecurityUtils.getCurrentOrgId();
        return new ResponseEntity<>(categoryService.createCategory(orgId, dto), HttpStatus.CREATED);
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<TicketCategoryDTO> updateCategory(@PathVariable Long id,
                                                            @RequestBody TicketCategoryDTO dto) {
        return ResponseEntity.ok(categoryService.updateCategory(id, dto));
    }

    // SLA Policies Management
    @GetMapping("/sla-policies")
    public ResponseEntity<List<SlaPolicyDTO>> listSlaPolicies() {
        Long orgId = SecurityUtils.getCurrentOrgId();
        return ResponseEntity.ok(slaPolicyService.getPoliciesByOrganization(orgId));
    }

    @PostMapping("/sla-policies")
    public ResponseEntity<SlaPolicyDTO> createSlaPolicy(@Valid @RequestBody SlaPolicyCreateRequest request) {
        Long orgId = SecurityUtils.getCurrentOrgId();
        return new ResponseEntity<>(slaPolicyService.createPolicy(orgId, request), HttpStatus.CREATED);
    }

    @PutMapping("/sla-policies/{id}")
    public ResponseEntity<SlaPolicyDTO> updateSlaPolicy(@PathVariable Long id,
                                                        @Valid @RequestBody SlaPolicyCreateRequest request) {
        return ResponseEntity.ok(slaPolicyService.updatePolicy(id, request));
    }

    // Assignment Rules Management
    @GetMapping("/assignment-rules")
    public ResponseEntity<List<AssignmentRuleDTO>> listRules() {
        Long orgId = SecurityUtils.getCurrentOrgId();
        return ResponseEntity.ok(assignmentRuleService.getRulesByOrganization(orgId));
    }

    @PostMapping("/assignment-rules")
    public ResponseEntity<AssignmentRuleDTO> createRule(@Valid @RequestBody AssignmentRuleCreateRequest request) {
        Long orgId = SecurityUtils.getCurrentOrgId();
        return new ResponseEntity<>(assignmentRuleService.createRule(orgId, request), HttpStatus.CREATED);
    }

    @PutMapping("/assignment-rules/{id}")
    public ResponseEntity<AssignmentRuleDTO> updateRule(@PathVariable Long id,
                                                        @RequestBody AssignmentRuleCreateRequest request) {
        return ResponseEntity.ok(assignmentRuleService.updateRule(id, request));
    }

    @DeleteMapping("/assignment-rules/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable Long id) {
        assignmentRuleService.deleteRule(id);
        return ResponseEntity.noContent().build();
    }

    // Dashboard & Chart Reports
    @GetMapping("/dashboard/stats")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats() {
        Long orgId = SecurityUtils.getCurrentOrgId();
        return ResponseEntity.ok(dashboardService.getDashboardStats(orgId));
    }

    @GetMapping("/dashboard/tickets-by-status")
    public ResponseEntity<List<TicketsByStatusDTO>> getTicketsByStatus() {
        Long orgId = SecurityUtils.getCurrentOrgId();
        return ResponseEntity.ok(dashboardService.getTicketsByStatus(orgId));
    }

    @GetMapping("/dashboard/tickets-by-priority")
    public ResponseEntity<List<TicketsByPriorityDTO>> getTicketsByPriority() {
        Long orgId = SecurityUtils.getCurrentOrgId();
        return ResponseEntity.ok(dashboardService.getTicketsByPriority(orgId));
    }

    @GetMapping("/dashboard/tickets-by-domain")
    public ResponseEntity<List<TicketsByDomainDTO>> getTicketsByDomain() {
        Long orgId = SecurityUtils.getCurrentOrgId();
        return ResponseEntity.ok(dashboardService.getTicketsByDomain(orgId));
    }

    @GetMapping("/dashboard/ticket-trends")
    public ResponseEntity<List<TicketTrendDTO>> getTicketTrends(@RequestParam(defaultValue = "14") int days) {
        Long orgId = SecurityUtils.getCurrentOrgId();
        return ResponseEntity.ok(dashboardService.getTicketTrends(orgId, days));
    }

    @GetMapping("/dashboard/sla-compliance")
    public ResponseEntity<List<SlaComplianceDTO>> getSlaCompliance(@RequestParam(defaultValue = "14") int days) {
        Long orgId = SecurityUtils.getCurrentOrgId();
        return ResponseEntity.ok(dashboardService.getSlaComplianceTrends(orgId, days));
    }

    // CSV Export
    @GetMapping(value = "/reports/export-csv", produces = "text/csv")
    public ResponseEntity<String> exportCsv(@RequestParam(required = false) String status,
                                            @RequestParam(required = false) String priority,
                                            @RequestParam(required = false) String domain) {
        Long orgId = SecurityUtils.getCurrentOrgId();
        String csv = reportService.exportTicketsCsv(orgId, status, priority, domain);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"supportdesk-tickets.csv\"");
        return new ResponseEntity<>(csv, headers, HttpStatus.OK);
    }
}

