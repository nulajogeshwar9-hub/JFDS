package com.supportdesk.service;

import com.supportdesk.dto.ClassificationSuggestionDTO;
import com.supportdesk.dto.TicketCreateRequest;
import com.supportdesk.dto.TicketDTO;
import com.supportdesk.entity.*;
import com.supportdesk.exception.BadRequestException;
import com.supportdesk.exception.UnauthorizedAccessException;
import com.supportdesk.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private TicketCategoryRepository categoryRepository;

    @Mock
    private SupportTeamRepository teamRepository;

    @Mock
    private ClassificationService classificationService;

    @Mock
    private AssignmentService assignmentService;

    @Mock
    private SlaService slaService;

    @Mock
    private TicketHistoryService historyService;

    @Mock
    private TicketReplyService replyService;

    @Mock
    private EmailNotificationService emailService;

    private TicketService ticketService;

    @BeforeEach
    void setUp() {
        ticketService = new TicketService(
                ticketRepository,
                userRepository,
                organizationRepository,
                categoryRepository,
                teamRepository,
                classificationService,
                assignmentService,
                slaService,
                historyService,
                replyService,
                emailService
        );
    }

    @Test
    void testCreateTicketGeneratesValidTicketNumber() {
        Organization org = Organization.builder().id(1L).domain("IT_SOFTWARE").build();
        User customer = User.builder().id(10L).firstName("John").lastName("Doe").organization(org).build();

        when(userRepository.findById(10L)).thenReturn(Optional.of(customer));
        when(ticketRepository.count()).thenReturn(42L);
        when(classificationService.classify(anyLong(), any(), any(), any()))
                .thenReturn(ClassificationSuggestionDTO.builder().suggestedPriority(Priority.MEDIUM).build());

        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket t = invocation.getArgument(0);
            t.setId(100L);
            return t;
        });

        TicketCreateRequest request = TicketCreateRequest.builder()
                .subject("Database connection lost")
                .description("Service threw timeout exception at 14:00")
                .domain("IT_SOFTWARE")
                .build();

        TicketDTO result = ticketService.createTicket(request, 10L);

        assertNotNull(result);
        assertTrue(result.getTicketNumber().matches("SD-\\d{4}-\\d{5}"));
        assertEquals("Database connection lost", result.getSubject());
        assertEquals(TicketStatus.OPEN, result.getStatus());
        verify(emailService, times(1)).sendTicketCreated(any());
    }

    @Test
    void testUpdateTicketStatusToResolvedSetsResolutionTimestamp() {
        Organization org = Organization.builder().id(1L).build();
        User agent = User.builder().id(2L).role(UserRole.AGENT).organization(org).build();
        User customer = User.builder().id(1L).role(UserRole.CUSTOMER).organization(org).build();

        Ticket ticket = Ticket.builder()
                .id(50L)
                .ticketNumber("SD-2026-00050")
                .organization(org)
                .customer(customer)
                .status(TicketStatus.IN_PROGRESS)
                .build();

        when(ticketRepository.findById(50L)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(2L)).thenReturn(Optional.of(agent));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(i -> i.getArgument(0));

        TicketDTO updated = ticketService.updateTicketStatus(50L, TicketStatus.RESOLVED, 2L, "Fix deployed");

        assertEquals(TicketStatus.RESOLVED, updated.getStatus());
        assertNotNull(ticket.getResolvedAt());
        verify(emailService, times(1)).sendResolutionNotification(ticket);
    }
}

