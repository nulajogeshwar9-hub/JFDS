package com.supportdesk.service;

import com.supportdesk.entity.Organization;
import com.supportdesk.entity.Priority;
import com.supportdesk.entity.SlaPolicy;
import com.supportdesk.entity.Ticket;
import com.supportdesk.repository.SlaBreachRepository;
import com.supportdesk.repository.SlaPolicyRepository;
import com.supportdesk.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SlaServiceTest {

    @Mock
    private SlaPolicyRepository slaPolicyRepository;

    @Mock
    private SlaBreachRepository slaBreachRepository;

    @Mock
    private TicketRepository ticketRepository;

    private SlaService slaService;

    @BeforeEach
    void setUp() {
        slaService = new SlaService(slaPolicyRepository, slaBreachRepository, ticketRepository);
    }

    @Test
    void testCalculateDeadlineWithinSameBusinessDay() {
        LocalTime start = LocalTime.of(9, 0);
        LocalTime end = LocalTime.of(17, 0);
        // Monday 10:00 AM
        LocalDateTime startDateTime = LocalDateTime.of(2026, 3, 2, 10, 0);

        LocalDateTime deadline = slaService.calculateDeadline(startDateTime, 120, start, end); // 2 hours

        assertEquals(LocalDateTime.of(2026, 3, 2, 12, 0), deadline);
    }

    @Test
    void testCalculateDeadlineCarryingOverNight() {
        LocalTime start = LocalTime.of(9, 0);
        LocalTime end = LocalTime.of(17, 0);
        // Monday 16:00 (4:00 PM) - 1 hour before end of day
        LocalDateTime startDateTime = LocalDateTime.of(2026, 3, 2, 16, 0);

        // Add 120 minutes (2 hours): 1 hour remaining today + 1 hour tomorrow morning
        LocalDateTime deadline = slaService.calculateDeadline(startDateTime, 120, start, end);

        // Should land on Tuesday at 10:00 AM
        assertEquals(LocalDateTime.of(2026, 3, 3, 10, 0), deadline);
    }

    @Test
    void testCalculateDeadlineSkippingWeekend() {
        LocalTime start = LocalTime.of(9, 0);
        LocalTime end = LocalTime.of(17, 0);
        // Friday 16:30 (4:30 PM) - 30 minutes before end of day
        LocalDateTime startDateTime = LocalDateTime.of(2026, 3, 6, 16, 30);

        // Add 60 minutes: 30 minutes Friday + 30 minutes Monday morning
        LocalDateTime deadline = slaService.calculateDeadline(startDateTime, 60, start, end);

        // Should land on Monday at 09:30 AM
        assertEquals(DayOfWeek.MONDAY, deadline.getDayOfWeek());
        assertEquals(LocalDateTime.of(2026, 3, 9, 9, 30), deadline);
    }

    @Test
    void testApplySlaPolicyToTicket() {
        Organization org = Organization.builder()
                .id(1L)
                .businessHoursStart(LocalTime.of(9, 0))
                .businessHoursEnd(LocalTime.of(17, 0))
                .build();

        SlaPolicy policy = SlaPolicy.builder()
                .id(100L)
                .name("Urgent SLA")
                .priority(Priority.URGENT)
                .firstResponseMinutes(15)
                .resolutionMinutes(240)
                .organization(org)
                .build();

        when(slaPolicyRepository.findByOrganizationIdAndPriority(1L, Priority.URGENT))
                .thenReturn(Optional.of(policy));

        Ticket ticket = Ticket.builder()
                .organization(org)
                .priority(Priority.URGENT)
                .createdAt(LocalDateTime.of(2026, 3, 2, 10, 0))
                .build();

        slaService.applySlaPolicy(ticket);

        assertNotNull(ticket.getFirstResponseDeadline());
        assertNotNull(ticket.getResolutionDeadline());
        assertEquals(policy, ticket.getSlaPolicy());
    }
}

