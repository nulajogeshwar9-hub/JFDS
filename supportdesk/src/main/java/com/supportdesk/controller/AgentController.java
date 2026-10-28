package com.supportdesk.controller;

import com.supportdesk.dto.DashboardStatsDTO;
import com.supportdesk.dto.PageDTO;
import com.supportdesk.dto.TicketDTO;
import com.supportdesk.dto.TicketSearchRequest;
import com.supportdesk.entity.Ticket;
import com.supportdesk.entity.TicketStatus;
import com.supportdesk.repository.TicketRepository;
import com.supportdesk.security.SecurityUtils;
import com.supportdesk.service.TicketService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private final TicketService ticketService;
    private final TicketRepository ticketRepository;

    public AgentController(TicketService ticketService,
                           TicketRepository ticketRepository) {
        this.ticketService = ticketService;
        this.ticketRepository = ticketRepository;
    }

    @GetMapping("/tickets")
    public ResponseEntity<PageDTO<TicketDTO>> getMyTickets(TicketSearchRequest req) {
        Long orgId = SecurityUtils.getCurrentOrgId();
        Long agentId = SecurityUtils.getCurrentUserId();
        req.setAssignedAgentId(agentId);
        PageDTO<TicketDTO> page = ticketService.searchTickets(req, orgId, agentId);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/team-queue")
    public ResponseEntity<List<TicketDTO>> getTeamQueue() {
        Long orgId = SecurityUtils.getCurrentOrgId();
        SecurityUtils.validateOrgAccess(orgId);

        // Tickets in the organization that are unassigned
        List<Ticket> unassigned = ticketRepository.findUnassignedTickets(orgId);
        List<TicketDTO> dtos = unassigned.stream()
                .map(ticketService::mapToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/dashboard/stats")
    public ResponseEntity<DashboardStatsDTO> getAgentStats() {
        Long agentId = SecurityUtils.getCurrentUserId();
        Long orgId = SecurityUtils.getCurrentOrgId();
        SecurityUtils.validateOrgAccess(orgId);

        List<Ticket> myTickets = ticketRepository.findAll().stream()
                .filter(t -> t.getAssignedAgent() != null && t.getAssignedAgent().getId().equals(agentId))
                .toList();

        long total = myTickets.size();
        long open = myTickets.stream().filter(t -> t.getStatus() == TicketStatus.OPEN).count();
        long inProgress = myTickets.stream().filter(t -> t.getStatus() == TicketStatus.IN_PROGRESS).count();
        long waiting = myTickets.stream().filter(t -> t.getStatus() == TicketStatus.WAITING_FOR_CUSTOMER).count();
        long resolved = myTickets.stream().filter(t -> t.getStatus() == TicketStatus.RESOLVED).count();
        long closed = myTickets.stream().filter(t -> t.getStatus() == TicketStatus.CLOSED).count();

        LocalDateTime now = LocalDateTime.now();
        long overdue = myTickets.stream()
                .filter(t -> t.getStatus() != TicketStatus.RESOLVED && t.getStatus() != TicketStatus.CLOSED)
                .filter(t -> (t.getResolutionDeadline() != null && now.isAfter(t.getResolutionDeadline()))
                        || (t.getFirstResponseDeadline() != null && t.getFirstResponseAt() == null && now.isAfter(t.getFirstResponseDeadline()))
                        || Boolean.TRUE.equals(t.getSlaFirstResponseBreached())
                        || Boolean.TRUE.equals(t.getSlaResolutionBreached()))
                .count();

        return ResponseEntity.ok(DashboardStatsDTO.builder()
                .totalTickets(total)
                .openTickets(open)
                .inProgressTickets(inProgress)
                .waitingTickets(waiting)
                .resolvedTickets(resolved)
                .closedTickets(closed)
                .overdueTickets(overdue)
                .build());
    }
}

