package com.supportdesk.service;

import com.supportdesk.entity.*;
import com.supportdesk.repository.SlaBreachRepository;
import com.supportdesk.repository.SlaPolicyRepository;
import com.supportdesk.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class SlaService {

    private static final Logger log = LoggerFactory.getLogger(SlaService.class);

    private final SlaPolicyRepository slaPolicyRepository;
    private final SlaBreachRepository slaBreachRepository;
    private final TicketRepository ticketRepository;

    public SlaService(SlaPolicyRepository slaPolicyRepository,
                      SlaBreachRepository slaBreachRepository,
                      TicketRepository ticketRepository) {
        this.slaPolicyRepository = slaPolicyRepository;
        this.slaBreachRepository = slaBreachRepository;
        this.ticketRepository = ticketRepository;
    }

    @Transactional
    public void applySlaPolicy(Ticket ticket) {
        if (ticket == null || ticket.getOrganization() == null || ticket.getPriority() == null) {
            return;
        }

        Long orgId = ticket.getOrganization().getId();
        Optional<SlaPolicy> policyOpt = slaPolicyRepository.findByOrganizationIdAndPriority(orgId, ticket.getPriority());

        if (policyOpt.isEmpty()) {
            policyOpt = slaPolicyRepository.findByOrganizationId(orgId).stream().findFirst();
        }

        if (policyOpt.isPresent()) {
            SlaPolicy policy = policyOpt.get();
            ticket.setSlaPolicy(policy);

            LocalDateTime now = ticket.getCreatedAt() != null ? ticket.getCreatedAt() : LocalDateTime.now();
            Organization org = ticket.getOrganization();

            LocalTime startHours = org.getBusinessHoursStart() != null ? org.getBusinessHoursStart() : LocalTime.of(9, 0);
            LocalTime endHours = org.getBusinessHoursEnd() != null ? org.getBusinessHoursEnd() : LocalTime.of(17, 0);

            LocalDateTime firstResponseDeadline = calculateDeadline(now, policy.getFirstResponseMinutes(), startHours, endHours);
            LocalDateTime resolutionDeadline = calculateDeadline(now, policy.getResolutionMinutes(), startHours, endHours);

            ticket.setFirstResponseDeadline(firstResponseDeadline);
            ticket.setResolutionDeadline(resolutionDeadline);
        }
    }

    public LocalDateTime calculateDeadline(LocalDateTime startTime, int minutesToAdd, LocalTime startHours, LocalTime endHours) {
        LocalDateTime current = startTime;
        int remainingMinutes = minutesToAdd;

        while (remainingMinutes > 0) {
            // Skip weekends
            if (current.getDayOfWeek() == DayOfWeek.SATURDAY) {
                current = current.plusDays(2).with(startHours);
                continue;
            } else if (current.getDayOfWeek() == DayOfWeek.SUNDAY) {
                current = current.plusDays(1).with(startHours);
                continue;
            }

            LocalTime currentTime = current.toLocalTime();

            if (currentTime.isBefore(startHours)) {
                current = current.with(startHours);
                continue;
            }

            if (currentTime.isAfter(endHours) || currentTime.equals(endHours)) {
                current = current.plusDays(1).with(startHours);
                continue;
            }

            long minutesUntilEndOfDay = Duration.between(current.toLocalTime(), endHours).toMinutes();

            if (remainingMinutes <= minutesUntilEndOfDay) {
                current = current.plusMinutes(remainingMinutes);
                remainingMinutes = 0;
            } else {
                remainingMinutes -= minutesUntilEndOfDay;
                current = current.plusDays(1).with(startHours);
            }
        }

        return current;
    }

    public Long calculateRemainingMinutes(LocalDateTime deadline) {
        if (deadline == null) return null;
        LocalDateTime now = LocalDateTime.now();
        return Duration.between(now, deadline).toMinutes();
    }

    @Scheduled(fixedRate = 300000) // Every 5 minutes
    @Transactional
    public void checkBreaches() {
        LocalDateTime now = LocalDateTime.now();
        List<Ticket> openTickets = ticketRepository.findAll().stream()
                .filter(t -> t.getStatus() != TicketStatus.RESOLVED && t.getStatus() != TicketStatus.CLOSED)
                .toList();

        for (Ticket ticket : openTickets) {
            // First response breach check
            if (Boolean.FALSE.equals(ticket.getSlaFirstResponseBreached())
                    && ticket.getFirstResponseAt() == null
                    && ticket.getFirstResponseDeadline() != null
                    && now.isAfter(ticket.getFirstResponseDeadline())) {
                ticket.setSlaFirstResponseBreached(true);
                ticketRepository.save(ticket);

                if (ticket.getSlaPolicy() != null) {
                    SlaBreach breach = SlaBreach.builder()
                            .ticket(ticket)
                            .slaPolicy(ticket.getSlaPolicy())
                            .breachType(BreachType.FIRST_RESPONSE)
                            .deadline(ticket.getFirstResponseDeadline())
                            .breachedAt(now)
                            .build();
                    slaBreachRepository.save(breach);
                    log.warn("SLA FIRST_RESPONSE breached for ticket {}", ticket.getTicketNumber());
                }
            }

            // Resolution breach check
            if (Boolean.FALSE.equals(ticket.getSlaResolutionBreached())
                    && ticket.getResolvedAt() == null
                    && ticket.getResolutionDeadline() != null
                    && now.isAfter(ticket.getResolutionDeadline())) {
                ticket.setSlaResolutionBreached(true);
                ticketRepository.save(ticket);

                if (ticket.getSlaPolicy() != null) {
                    SlaBreach breach = SlaBreach.builder()
                            .ticket(ticket)
                            .slaPolicy(ticket.getSlaPolicy())
                            .breachType(BreachType.RESOLUTION)
                            .deadline(ticket.getResolutionDeadline())
                            .breachedAt(now)
                            .build();
                    slaBreachRepository.save(breach);
                    log.warn("SLA RESOLUTION breached for ticket {}", ticket.getTicketNumber());
                }
            }
        }
    }
}

