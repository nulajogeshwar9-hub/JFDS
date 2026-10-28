package com.supportdesk.service;

import com.supportdesk.dto.DashboardStatsDTO;
import com.supportdesk.dto.TicketsByStatusDTO;
import com.supportdesk.entity.Organization;
import com.supportdesk.entity.Priority;
import com.supportdesk.entity.Ticket;
import com.supportdesk.entity.TicketStatus;
import com.supportdesk.repository.TicketRepository;
import com.supportdesk.security.CustomUserDetails;
import com.supportdesk.entity.User;
import com.supportdesk.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardService(ticketRepository);

        // Setup Security Context with Admin User in Org 1
        Organization org = Organization.builder().id(1L).build();
        User adminUser = User.builder().id(99L).email("admin@test.com").role(UserRole.ADMIN).organization(org).active(true).build();
        CustomUserDetails userDetails = new CustomUserDetails(adminUser);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void testGetDashboardStatsAggregations() {
        Organization org = Organization.builder().id(1L).build();

        Ticket t1 = Ticket.builder()
                .id(1L)
                .organization(org)
                .status(TicketStatus.OPEN)
                .priority(Priority.HIGH)
                .createdAt(LocalDateTime.now().minusHours(5))
                .build();

        Ticket t2 = Ticket.builder()
                .id(2L)
                .organization(org)
                .status(TicketStatus.RESOLVED)
                .priority(Priority.MEDIUM)
                .createdAt(LocalDateTime.now().minusHours(10))
                .firstResponseAt(LocalDateTime.now().minusHours(8))
                .resolvedAt(LocalDateTime.now().minusHours(4))
                .build();

        when(ticketRepository.findAll()).thenReturn(List.of(t1, t2));

        DashboardStatsDTO stats = dashboardService.getDashboardStats(1L);

        assertNotNull(stats);
        assertEquals(2, stats.getTotalTickets());
        assertEquals(1, stats.getOpenTickets());
        assertEquals(1, stats.getResolvedTickets());
        assertTrue(stats.getSlaComplianceRate() >= 0);
    }

    @Test
    void testGetTicketsByStatusReturnsAllStatuses() {
        Organization org = Organization.builder().id(1L).build();
        Ticket t1 = Ticket.builder().id(1L).organization(org).status(TicketStatus.OPEN).build();

        when(ticketRepository.findAll()).thenReturn(List.of(t1));

        List<TicketsByStatusDTO> list = dashboardService.getTicketsByStatus(1L);

        assertNotNull(list);
        assertEquals(TicketStatus.values().length, list.size());
        assertEquals(1, list.stream().filter(s -> s.getStatus() == TicketStatus.OPEN).findFirst().get().getCount());
    }
}

