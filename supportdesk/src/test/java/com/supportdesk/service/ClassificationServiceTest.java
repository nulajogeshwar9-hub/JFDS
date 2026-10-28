package com.supportdesk.service;

import com.supportdesk.dto.ClassificationSuggestionDTO;
import com.supportdesk.entity.Priority;
import com.supportdesk.entity.SupportTeam;
import com.supportdesk.entity.TicketCategory;
import com.supportdesk.repository.SupportTeamRepository;
import com.supportdesk.repository.TicketCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClassificationServiceTest {

    @Mock
    private TicketCategoryRepository categoryRepository;

    @Mock
    private SupportTeamRepository teamRepository;

    private RuleBasedClassificationService classificationService;

    @BeforeEach
    void setUp() {
        classificationService = new RuleBasedClassificationService(categoryRepository, teamRepository);

        TicketCategory loginCat = TicketCategory.builder().id(1L).name("Login Issues").build();
        TicketCategory billingCat = TicketCategory.builder().id(2L).name("Payment Issues").build();
        TicketCategory bugCat = TicketCategory.builder().id(3L).name("Bug Report").build();
        TicketCategory generalCat = TicketCategory.builder().id(4L).name("General Inquiry").build();

        SupportTeam techTeam = SupportTeam.builder().id(10L).name("Technical Support").build();
        SupportTeam billingTeam = SupportTeam.builder().id(20L).name("Billing Support").build();
        SupportTeam generalTeam = SupportTeam.builder().id(30L).name("General Support").build();

        lenient().when(categoryRepository.findByOrganizationId(1L)).thenReturn(List.of(loginCat, billingCat, bugCat, generalCat));
        lenient().when(teamRepository.findByOrganizationId(1L)).thenReturn(List.of(techTeam, billingTeam, generalTeam));
    }

    @Test
    void testClassifyLoginFailure() {
        ClassificationSuggestionDTO result = classificationService.classify(
                1L, "Cannot login to portal", "Password reset link is broken", "IT_SOFTWARE");

        assertNotNull(result);
        assertEquals("Login Issues", result.getSuggestedCategoryName());
        assertEquals("Technical Support", result.getSuggestedTeamName());
        assertTrue(result.getConfidenceScore() >= 0.7);
    }

    @Test
    void testClassifyRefundRequest() {
        ClassificationSuggestionDTO result = classificationService.classify(
                1L, "Double charge on invoice", "Need refund for last month transaction", "ECOMMERCE");

        assertNotNull(result);
        assertEquals("Payment Issues", result.getSuggestedCategoryName());
        assertEquals("Billing Support", result.getSuggestedTeamName());
        assertEquals(Priority.HIGH, result.getSuggestedPriority());
    }

    @Test
    void testClassifyUrgentOutageEscalation() {
        ClassificationSuggestionDTO result = classificationService.classify(
                1L, "Critical outage system down", "Production database is not responding", "IT_SOFTWARE");

        assertNotNull(result);
        assertEquals(Priority.URGENT, result.getSuggestedPriority());
        assertEquals(0.95, result.getConfidenceScore());
    }

    @Test
    void testClassifyDefaultInquiry() {
        ClassificationSuggestionDTO result = classificationService.classify(
                1L, "Question about working hours", "Where can I find holiday calendar?", "GENERAL");

        assertNotNull(result);
        assertEquals(Priority.MEDIUM, result.getSuggestedPriority());
        assertEquals("General Support", result.getSuggestedTeamName());
    }
}

