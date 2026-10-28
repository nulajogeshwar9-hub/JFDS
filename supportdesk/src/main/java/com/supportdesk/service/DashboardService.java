package com.supportdesk.service;

import com.supportdesk.dto.*;
import com.supportdesk.entity.Priority;
import com.supportdesk.entity.Ticket;
import com.supportdesk.entity.TicketStatus;
import com.supportdesk.repository.TicketRepository;
import com.supportdesk.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final TicketRepository ticketRepository;

    public DashboardService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Transactional(readOnly = true)
    public DashboardStatsDTO getDashboardStats(Long orgId) {
        SecurityUtils.validateOrgAccess(orgId);

        List<Ticket> allTickets = ticketRepository.findAll().stream()
                .filter(t -> t.getOrganization().getId().equals(orgId))
                .toList();

        long total = allTickets.size();
        long open = allTickets.stream().filter(t -> t.getStatus() == TicketStatus.OPEN).count();
        long inProgress = allTickets.stream().filter(t -> t.getStatus() == TicketStatus.IN_PROGRESS).count();
        long waiting = allTickets.stream().filter(t -> t.getStatus() == TicketStatus.WAITING_FOR_CUSTOMER).count();
        long resolved = allTickets.stream().filter(t -> t.getStatus() == TicketStatus.RESOLVED).count();
        long closed = allTickets.stream().filter(t -> t.getStatus() == TicketStatus.CLOSED).count();

        LocalDateTime now = LocalDateTime.now();
        long overdue = allTickets.stream()
                .filter(t -> t.getStatus() != TicketStatus.RESOLVED && t.getStatus() != TicketStatus.CLOSED)
                .filter(t -> (t.getResolutionDeadline() != null && now.isAfter(t.getResolutionDeadline()))
                        || (t.getFirstResponseDeadline() != null && t.getFirstResponseAt() == null && now.isAfter(t.getFirstResponseDeadline()))
                        || Boolean.TRUE.equals(t.getSlaFirstResponseBreached())
                        || Boolean.TRUE.equals(t.getSlaResolutionBreached()))
                .count();

        // Calculate average first response time in minutes
        List<Ticket> respondedTickets = allTickets.stream()
                .filter(t -> t.getFirstResponseAt() != null && t.getCreatedAt() != null)
                .toList();
        double avgFirstResponseMinutes = respondedTickets.isEmpty() ? 0.0 :
                respondedTickets.stream()
                        .mapToLong(t -> Math.max(0, Duration.between(t.getCreatedAt(), t.getFirstResponseAt()).toMinutes()))
                        .average()
                        .orElse(0.0);

        // Calculate average resolution time in minutes
        List<Ticket> resolvedTicketsList = allTickets.stream()
                .filter(t -> t.getResolvedAt() != null && t.getCreatedAt() != null)
                .toList();
        double avgResolutionMinutes = resolvedTicketsList.isEmpty() ? 0.0 :
                resolvedTicketsList.stream()
                        .mapToLong(t -> Math.max(0, Duration.between(t.getCreatedAt(), t.getResolvedAt()).toMinutes()))
                        .average()
                        .orElse(0.0);

        // Calculate SLA compliance rate
        long breachedCount = allTickets.stream()
                .filter(t -> Boolean.TRUE.equals(t.getSlaFirstResponseBreached()) || Boolean.TRUE.equals(t.getSlaResolutionBreached()))
                .count();
        double complianceRate = total == 0 ? 100.0 : Math.round(((total - breachedCount) * 100.0 / total) * 10.0) / 10.0;

        return DashboardStatsDTO.builder()
                .totalTickets(total)
                .openTickets(open)
                .inProgressTickets(inProgress)
                .waitingTickets(waiting)
                .resolvedTickets(resolved)
                .closedTickets(closed)
                .overdueTickets(overdue)
                .avgFirstResponseMinutes(Math.round(avgFirstResponseMinutes * 10.0) / 10.0)
                .avgResolutionMinutes(Math.round(avgResolutionMinutes * 10.0) / 10.0)
                .slaComplianceRate(complianceRate)
                .build();
    }

    @Transactional(readOnly = true)
    public List<TicketsByStatusDTO> getTicketsByStatus(Long orgId) {
        SecurityUtils.validateOrgAccess(orgId);
        List<Ticket> tickets = ticketRepository.findAll().stream()
                .filter(t -> t.getOrganization().getId().equals(orgId))
                .toList();

        Map<TicketStatus, Long> counts = tickets.stream()
                .collect(Collectors.groupingBy(Ticket::getStatus, Collectors.counting()));

        List<TicketsByStatusDTO> list = new ArrayList<>();
        for (TicketStatus status : TicketStatus.values()) {
            list.add(new TicketsByStatusDTO(status, counts.getOrDefault(status, 0L)));
        }
        return list;
    }

    @Transactional(readOnly = true)
    public List<TicketsByPriorityDTO> getTicketsByPriority(Long orgId) {
        SecurityUtils.validateOrgAccess(orgId);
        List<Ticket> tickets = ticketRepository.findAll().stream()
                .filter(t -> t.getOrganization().getId().equals(orgId))
                .toList();

        Map<Priority, Long> counts = tickets.stream()
                .collect(Collectors.groupingBy(Ticket::getPriority, Collectors.counting()));

        List<TicketsByPriorityDTO> list = new ArrayList<>();
        for (Priority priority : Priority.values()) {
            list.add(new TicketsByPriorityDTO(priority, counts.getOrDefault(priority, 0L)));
        }
        return list;
    }

    @Transactional(readOnly = true)
    public List<TicketsByDomainDTO> getTicketsByDomain(Long orgId) {
        SecurityUtils.validateOrgAccess(orgId);
        List<Ticket> tickets = ticketRepository.findAll().stream()
                .filter(t -> t.getOrganization().getId().equals(orgId))
                .toList();

        Map<String, Long> counts = tickets.stream()
                .collect(Collectors.groupingBy(t -> t.getDomain() != null ? t.getDomain() : "General", Collectors.counting()));

        return counts.entrySet().stream()
                .map(e -> new TicketsByDomainDTO(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TicketTrendDTO> getTicketTrends(Long orgId, int days) {
        SecurityUtils.validateOrgAccess(orgId);
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<Ticket> tickets = ticketRepository.findAll().stream()
                .filter(t -> t.getOrganization().getId().equals(orgId))
                .filter(t -> t.getCreatedAt() != null && t.getCreatedAt().isAfter(since))
                .toList();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Map<String, Long> createdMap = tickets.stream()
                .collect(Collectors.groupingBy(t -> t.getCreatedAt().format(fmt), Collectors.counting()));

        Map<String, Long> resolvedMap = tickets.stream()
                .filter(t -> t.getResolvedAt() != null)
                .collect(Collectors.groupingBy(t -> t.getResolvedAt().format(fmt), Collectors.counting()));

        List<TicketTrendDTO> result = new ArrayList<>();
        LocalDate start = LocalDate.now().minusDays(days - 1);
        for (int i = 0; i < days; i++) {
            LocalDate d = start.plusDays(i);
            String dateStr = d.format(fmt);
            result.add(TicketTrendDTO.builder()
                    .date(dateStr)
                    .createdCount(createdMap.getOrDefault(dateStr, 0L))
                    .resolvedCount(resolvedMap.getOrDefault(dateStr, 0L))
                    .build());
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<SlaComplianceDTO> getSlaComplianceTrends(Long orgId, int days) {
        SecurityUtils.validateOrgAccess(orgId);
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<Ticket> tickets = ticketRepository.findAll().stream()
                .filter(t -> t.getOrganization().getId().equals(orgId))
                .filter(t -> t.getCreatedAt() != null && t.getCreatedAt().isAfter(since))
                .toList();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Map<String, List<Ticket>> grouped = tickets.stream()
                .collect(Collectors.groupingBy(t -> t.getCreatedAt().format(fmt)));

        List<SlaComplianceDTO> result = new ArrayList<>();
        LocalDate start = LocalDate.now().minusDays(days - 1);
        for (int i = 0; i < days; i++) {
            LocalDate d = start.plusDays(i);
            String dateStr = d.format(fmt);
            List<Ticket> dayTickets = grouped.getOrDefault(dateStr, Collections.emptyList());
            long total = dayTickets.size();
            long breached = dayTickets.stream()
                    .filter(t -> Boolean.TRUE.equals(t.getSlaFirstResponseBreached()) || Boolean.TRUE.equals(t.getSlaResolutionBreached()))
                    .count();
            long within = total - breached;
            double rate = total == 0 ? 100.0 : Math.round((within * 100.0 / total) * 10.0) / 10.0;

            result.add(SlaComplianceDTO.builder()
                    .period(dateStr)
                    .totalTickets(total)
                    .withinSlaCount(within)
                    .breachedCount(breached)
                    .complianceRate(rate)
                    .build());
        }
        return result;
    }
}

