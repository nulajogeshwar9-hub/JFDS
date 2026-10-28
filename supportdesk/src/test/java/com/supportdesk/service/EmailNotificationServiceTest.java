package com.supportdesk.service;

import com.supportdesk.entity.Organization;
import com.supportdesk.entity.Priority;
import com.supportdesk.entity.Ticket;
import com.supportdesk.entity.TicketStatus;
import com.supportdesk.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EmailNotificationServiceTest {

    @Test
    void testEmailDisabledDoesNotThrowException() {
        // Mail sender is null (disabled by default in test/dev)
        EmailNotificationService service = new EmailNotificationService(null);

        assertFalse(service.isMailEnabled());

        Ticket ticket = Ticket.builder()
                .ticketNumber("SD-2026-00001")
                .subject("Test ticket")
                .priority(Priority.LOW)
                .status(TicketStatus.OPEN)
                .customer(User.builder().email("customer@example.com").firstName("Jane").lastName("Doe").build())
                .build();

        // All notification calls must be safe no-ops
        assertDoesNotThrow(() -> service.sendTicketCreated(ticket));
        assertDoesNotThrow(() -> service.sendTicketAssigned(ticket));
        assertDoesNotThrow(() -> service.sendStatusUpdate(ticket, "OPEN", "IN_PROGRESS"));
        assertDoesNotThrow(() -> service.sendAgentReply(ticket, "Investigating now"));
        assertDoesNotThrow(() -> service.sendResolutionNotification(ticket));
    }
}

