package com.supportdesk.service;

import com.supportdesk.dto.ClassificationSuggestionDTO;
import com.supportdesk.entity.Priority;
import com.supportdesk.entity.SupportTeam;
import com.supportdesk.entity.TicketCategory;
import com.supportdesk.repository.SupportTeamRepository;
import com.supportdesk.repository.TicketCategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class RuleBasedClassificationService implements ClassificationService {

    private static final Logger log = LoggerFactory.getLogger(RuleBasedClassificationService.class);

    private final TicketCategoryRepository categoryRepository;
    private final SupportTeamRepository teamRepository;

    public RuleBasedClassificationService(TicketCategoryRepository categoryRepository,
                                          SupportTeamRepository teamRepository) {
        this.categoryRepository = categoryRepository;
        this.teamRepository = teamRepository;
    }

    @Override
    public ClassificationSuggestionDTO classify(Long orgId, String subject, String description, String domain) {
        String combined = ((subject != null ? subject : "") + " " + (description != null ? description : "")).toLowerCase(Locale.ROOT);
        String domainLower = domain != null ? domain.toLowerCase(Locale.ROOT) : "";

        List<TicketCategory> categories = categoryRepository.findByOrganizationId(orgId);
        List<SupportTeam> teams = teamRepository.findByOrganizationId(orgId);

        String categoryKeyword = "General Inquiry";
        String teamKeyword = "General Support";
        Priority priority = Priority.MEDIUM;
        double confidence = 0.70;
        StringBuilder reasoning = new StringBuilder("Rule-based keyword matching: ");

        // Priority evaluation
        if (combined.contains("urgent") || combined.contains("emergency") || combined.contains("critical")
                || combined.contains("outage") || combined.contains("system down") || combined.contains("production down")) {
            priority = Priority.URGENT;
            confidence = Math.max(confidence, 0.95);
            reasoning.append("Critical/urgent keyword detected -> URGENT priority. ");
        } else if (combined.contains("broken") || combined.contains("error") || combined.contains("fail")
                || combined.contains("crash") || combined.contains("high priority") || combined.contains("refund")) {
            priority = Priority.HIGH;
            confidence = Math.max(confidence, 0.85);
            reasoning.append("High severity failure keyword detected -> HIGH priority. ");
        } else if (combined.contains("suggestion") || combined.contains("enhancement") || combined.contains("minor")
                || combined.contains("feature request")) {
            priority = Priority.LOW;
            reasoning.append("Informational/enhancement keyword detected -> LOW priority. ");
        }

        // Domain & Category evaluation
        if (domainLower.contains("education") || combined.contains("examination") || combined.contains("certificate")
                || combined.contains("grade") || combined.contains("student") || combined.contains("course")) {
            categoryKeyword = "Education Administration";
            teamKeyword = "General Support";
            reasoning.append("Educational domain keywords detected. ");
        } else if (domainLower.contains("health") || combined.contains("appointment") || combined.contains("patient")
                || combined.contains("doctor") || combined.contains("prescription") || combined.contains("clinic")) {
            categoryKeyword = "Healthcare Administration";
            teamKeyword = "General Support";
            reasoning.append("Healthcare domain keywords detected. ");
        } else if (combined.contains("login") || combined.contains("password") || combined.contains("authentication")
                || combined.contains("sign in") || combined.contains("2fa") || combined.contains("mfa") || combined.contains("access denied")) {
            categoryKeyword = "Login Issues";
            teamKeyword = "Technical Support";
            reasoning.append("Authentication/credentials issue -> Technical Support team. ");
        } else if (combined.contains("payment") || combined.contains("refund") || combined.contains("invoice")
                || combined.contains("billing") || combined.contains("charge") || combined.contains("subscription") || combined.contains("credit card")) {
            categoryKeyword = "Payment Issues";
            teamKeyword = "Billing Support";
            if (priority == Priority.MEDIUM) priority = Priority.HIGH;
            reasoning.append("Financial/billing transaction issue -> Billing Support team. ");
        } else if (combined.contains("bug") || combined.contains("crash") || combined.contains("exception")
                || combined.contains("nullpointer") || combined.contains("stack trace") || combined.contains("glitch")) {
            categoryKeyword = "Bug Report";
            teamKeyword = "Technical Support";
            reasoning.append("Software defect detected -> Technical Support team. ");
        } else if (combined.contains("feature") || combined.contains("request") || combined.contains("add support for")) {
            categoryKeyword = "Feature Request";
            teamKeyword = "General Support";
            reasoning.append("Product enhancement request -> General Support. ");
        }

        // Match category from DB
        final String catTarget = categoryKeyword;
        TicketCategory matchedCategory = categories.stream()
                .filter(c -> c.getName().equalsIgnoreCase(catTarget))
                .findFirst()
                .orElseGet(() -> categories.stream()
                        .filter(c -> c.getName().toLowerCase().contains("technical") && catTarget.toLowerCase().contains("tech")
                                || c.getName().toLowerCase().contains("general")
                                || c.getName().toLowerCase().contains("billing") && catTarget.toLowerCase().contains("pay"))
                        .findFirst()
                        .orElse(categories.isEmpty() ? null : categories.get(0)));

        // Match team from DB
        final String teamTarget = teamKeyword;
        SupportTeam matchedTeam = teams.stream()
                .filter(t -> t.getName().equalsIgnoreCase(teamTarget))
                .findFirst()
                .orElseGet(() -> teams.stream()
                        .filter(t -> t.getName().toLowerCase().contains("support"))
                        .findFirst()
                        .orElse(teams.isEmpty() ? null : teams.get(0)));

        return ClassificationSuggestionDTO.builder()
                .suggestedCategoryId(matchedCategory != null ? matchedCategory.getId() : null)
                .suggestedCategoryName(matchedCategory != null ? matchedCategory.getName() : categoryKeyword)
                .suggestedPriority(priority)
                .suggestedTeamId(matchedTeam != null ? matchedTeam.getId() : null)
                .suggestedTeamName(matchedTeam != null ? matchedTeam.getName() : teamKeyword)
                .confidenceScore(confidence)
                .reasoning(reasoning.toString())
                .build();
    }
}

