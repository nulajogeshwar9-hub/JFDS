package com.supportdesk.service;

import com.supportdesk.entity.Ticket;
import com.supportdesk.repository.TicketRepository;
import com.supportdesk.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReportService {

    private final TicketRepository ticketRepository;

    public ReportService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Transactional(readOnly = true)
    public String exportTicketsCsv(Long orgId, String status, String priority, String domain) {
        SecurityUtils.validateOrgAccess(orgId);

        List<Ticket> tickets = ticketRepository.findAll().stream()
                .filter(t -> t.getOrganization().getId().equals(orgId))
                .filter(t -> status == null || status.isBlank() || t.getStatus().name().equalsIgnoreCase(status))
                .filter(t -> priority == null || priority.isBlank() || t.getPriority().name().equalsIgnoreCase(priority))
                .filter(t -> domain == null || domain.isBlank() || (t.getDomain() != null && t.getDomain().equalsIgnoreCase(domain)))
                .toList();

        StringBuilder sb = new StringBuilder();
        // CSV Header
        sb.append("Ticket Number,Subject,Status,Priority,Domain,Category,Customer Name,Customer Email,Assigned Team,Assigned Agent,Created At,First Response At,Resolved At,SLA Breached\n");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (Ticket t : tickets) {
            boolean breached = Boolean.TRUE.equals(t.getSlaFirstResponseBreached()) || Boolean.TRUE.equals(t.getSlaResolutionBreached());
            sb.append(escapeCsv(t.getTicketNumber())).append(",")
                    .append(escapeCsv(t.getSubject())).append(",")
                    .append(escapeCsv(t.getStatus() != null ? t.getStatus().name() : "")).append(",")
                    .append(escapeCsv(t.getPriority() != null ? t.getPriority().name() : "")).append(",")
                    .append(escapeCsv(t.getDomain() != null ? t.getDomain() : "")).append(",")
                    .append(escapeCsv(t.getCategory() != null ? t.getCategory().getName() : "")).append(",")
                    .append(escapeCsv(t.getCustomer() != null ? t.getCustomer().getFullName() : "")).append(",")
                    .append(escapeCsv(t.getCustomer() != null ? t.getCustomer().getEmail() : "")).append(",")
                    .append(escapeCsv(t.getAssignedTeam() != null ? t.getAssignedTeam().getName() : "")).append(",")
                    .append(escapeCsv(t.getAssignedAgent() != null ? t.getAssignedAgent().getFullName() : "")).append(",")
                    .append(escapeCsv(t.getCreatedAt() != null ? t.getCreatedAt().format(fmt) : "")).append(",")
                    .append(escapeCsv(t.getFirstResponseAt() != null ? t.getFirstResponseAt().format(fmt) : "")).append(",")
                    .append(escapeCsv(t.getResolvedAt() != null ? t.getResolvedAt().format(fmt) : "")).append(",")
                    .append(breached ? "YES" : "NO").append("\n");
        }

        return sb.toString();
    }

    private String escapeCsv(String val) {
        if (val == null) return "\"\"";
        // Prevent CSV formula injection
        if (val.startsWith("=") || val.startsWith("+") || val.startsWith("-") || val.startsWith("@")) {
            val = "'" + val;
        }
        val = val.replace("\"", "\"\"");
        return "\"" + val + "\"";
    }
}

