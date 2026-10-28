package com.supportdesk.service;

import com.supportdesk.entity.*;
import com.supportdesk.repository.AssignmentRuleRepository;
import com.supportdesk.repository.SupportTeamRepository;
import com.supportdesk.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssignmentServiceTest {

    @Mock
    private AssignmentRuleRepository assignmentRuleRepository;

    @Mock
    private SupportTeamRepository supportTeamRepository;

    @Mock
    private UserRepository userRepository;

    private AssignmentService assignmentService;

    @BeforeEach
    void setUp() {
        assignmentService = new AssignmentService(assignmentRuleRepository, supportTeamRepository, userRepository);
    }

    @Test
    void testAutoAssignMatchingRuleFound() {
        Organization org = Organization.builder().id(1L).build();
        TicketCategory cat = TicketCategory.builder().id(5L).build();
        SupportTeam team = SupportTeam.builder().id(10L).name("Security Team").build();
        User agent = User.builder().id(20L).firstName("Sarah").lastName("Connor").active(true).build();

        AssignmentRule rule = AssignmentRule.builder()
                .id(1L)
                .name("Security High Priority")
                .organization(org)
                .domain("IT_SOFTWARE")
                .category(cat)
                .priority(Priority.HIGH)
                .assignTeam(team)
                .assignAgent(agent)
                .active(true)
                .build();

        when(assignmentRuleRepository.findMatchingRules(1L, "IT_SOFTWARE", 5L, Priority.HIGH))
                .thenReturn(List.of(rule));

        Ticket ticket = Ticket.builder()
                .ticketNumber("SD-2026-00001")
                .organization(org)
                .domain("IT_SOFTWARE")
                .category(cat)
                .priority(Priority.HIGH)
                .build();

        boolean assigned = assignmentService.autoAssign(ticket);

        assertTrue(assigned);
        assertEquals(team, ticket.getAssignedTeam());
        assertEquals(agent, ticket.getAssignedAgent());
    }

    @Test
    void testAutoAssignNoRuleMatchesLeavesUnassigned() {
        Organization org = Organization.builder().id(1L).build();

        when(assignmentRuleRepository.findMatchingRules(anyLong(), any(), any(), any()))
                .thenReturn(Collections.emptyList());

        Ticket ticket = Ticket.builder()
                .ticketNumber("SD-2026-00002")
                .organization(org)
                .domain("GENERAL")
                .priority(Priority.LOW)
                .build();

        boolean assigned = assignmentService.autoAssign(ticket);

        assertFalse(assigned);
        assertNull(ticket.getAssignedTeam());
        assertNull(ticket.getAssignedAgent());
    }
}

