package com.supportdesk.service;

import com.supportdesk.entity.AssignmentRule;
import com.supportdesk.entity.SupportTeam;
import com.supportdesk.entity.Ticket;
import com.supportdesk.entity.User;
import com.supportdesk.repository.AssignmentRuleRepository;
import com.supportdesk.repository.SupportTeamRepository;
import com.supportdesk.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AssignmentService {

    private static final Logger log = LoggerFactory.getLogger(AssignmentService.class);

    private final AssignmentRuleRepository assignmentRuleRepository;
    private final SupportTeamRepository supportTeamRepository;
    private final UserRepository userRepository;

    public AssignmentService(AssignmentRuleRepository assignmentRuleRepository,
                             SupportTeamRepository supportTeamRepository,
                             UserRepository userRepository) {
        this.assignmentRuleRepository = assignmentRuleRepository;
        this.supportTeamRepository = supportTeamRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public boolean autoAssign(Ticket ticket) {
        if (ticket == null || ticket.getOrganization() == null) {
            return false;
        }

        Long orgId = ticket.getOrganization().getId();
        String domain = ticket.getDomain();
        Long categoryId = ticket.getCategory() != null ? ticket.getCategory().getId() : null;

        List<AssignmentRule> rules = assignmentRuleRepository.findMatchingRules(
                orgId, domain, categoryId, ticket.getPriority());

        if (!rules.isEmpty()) {
            AssignmentRule matchedRule = rules.get(0);
            log.info("Ticket {} matched assignment rule '{}'", ticket.getTicketNumber(), matchedRule.getName());

            if (matchedRule.getAssignTeam() != null) {
                ticket.setAssignedTeam(matchedRule.getAssignTeam());
            }
            if (matchedRule.getAssignAgent() != null && Boolean.TRUE.equals(matchedRule.getAssignAgent().getActive())) {
                ticket.setAssignedAgent(matchedRule.getAssignAgent());
            }
            return true;
        }

        log.info("No assignment rule matched for ticket {}. Ticket remains in unassigned queue.", ticket.getTicketNumber());
        return false;
    }
}

