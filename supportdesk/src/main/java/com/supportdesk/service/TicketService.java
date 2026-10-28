package com.supportdesk.service;

import com.supportdesk.dto.*;
import com.supportdesk.entity.*;
import com.supportdesk.exception.BadRequestException;
import com.supportdesk.exception.ResourceNotFoundException;
import com.supportdesk.exception.UnauthorizedAccessException;
import com.supportdesk.repository.*;
import com.supportdesk.security.SecurityUtils;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final TicketCategoryRepository categoryRepository;
    private final SupportTeamRepository teamRepository;
    private final ClassificationService classificationService;
    private final AssignmentService assignmentService;
    private final SlaService slaService;
    private final TicketHistoryService historyService;
    private final TicketReplyService replyService;
    private final EmailNotificationService emailService;

    public TicketService(TicketRepository ticketRepository,
                         UserRepository userRepository,
                         OrganizationRepository organizationRepository,
                         TicketCategoryRepository categoryRepository,
                         SupportTeamRepository teamRepository,
                         ClassificationService classificationService,
                         AssignmentService assignmentService,
                         SlaService slaService,
                         TicketHistoryService historyService,
                         TicketReplyService replyService,
                         EmailNotificationService emailService) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.categoryRepository = categoryRepository;
        this.teamRepository = teamRepository;
        this.classificationService = classificationService;
        this.assignmentService = assignmentService;
        this.slaService = slaService;
        this.historyService = historyService;
        this.replyService = replyService;
        this.emailService = emailService;
    }

    @Transactional
    public TicketDTO createTicket(TicketCreateRequest request, Long customerUserId) {
        User customer = userRepository.findById(customerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerUserId));

        Organization org = customer.getOrganization();
        if (org == null) {
            org = organizationRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> new BadRequestException("No organization configured"));
        }

        // Rule-based Classification suggestion
        ClassificationSuggestionDTO suggestion = classificationService.classify(
                org.getId(), request.getSubject(), request.getDescription(), request.getDomain());

        TicketCategory category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId()).orElse(null);
        } else if (suggestion.getSuggestedCategoryId() != null) {
            category = categoryRepository.findById(suggestion.getSuggestedCategoryId()).orElse(null);
        }

        Priority priority = request.getPriority() != null ? request.getPriority() : suggestion.getSuggestedPriority();
        if (priority == null) priority = Priority.MEDIUM;

        String domain = request.getDomain() != null ? request.getDomain() : org.getDomain();

        String ticketNumber = generateTicketNumber();

        Ticket ticket = Ticket.builder()
                .ticketNumber(ticketNumber)
                .organization(org)
                .customer(customer)
                .subject(request.getSubject())
                .description(request.getDescription())
                .domain(domain)
                .category(category)
                .priority(priority)
                .status(TicketStatus.OPEN)
                .slaFirstResponseBreached(false)
                .slaResolutionBreached(false)
                .build();

        // Apply SLA policy deadlines based on priority & business hours
        slaService.applySlaPolicy(ticket);

        // Auto-assign based on configurable assignment rules
        assignmentService.autoAssign(ticket);

        ticket = ticketRepository.save(ticket);

        // Record History
        historyService.recordChange(ticket, customer, "CREATED", null, ticket.getStatus().name(), "Ticket submitted by customer");

        if (ticket.getAssignedTeam() != null) {
            historyService.recordChange(ticket, null, "ASSIGNED_TEAM", null, ticket.getAssignedTeam().getName(), "Auto-assigned via rules");
        }
        if (ticket.getAssignedAgent() != null) {
            historyService.recordChange(ticket, null, "ASSIGNED_AGENT", null, ticket.getAssignedAgent().getFullName(), "Auto-assigned via rules");
        }

        // Send Email notification
        emailService.sendTicketCreated(ticket);
        if (ticket.getAssignedAgent() != null) {
            emailService.sendTicketAssigned(ticket);
        }

        return mapToDTO(ticket);
    }

    @Transactional(readOnly = true)
    public TicketDetailDTO getTicketDetail(Long id, Long currentUserId) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + id));

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserId));

        SecurityUtils.validateOrgAccess(ticket.getOrganization().getId());

        if (currentUser.getRole() == UserRole.CUSTOMER && !ticket.getCustomer().getId().equals(currentUser.getId())) {
            throw new UnauthorizedAccessException("You cannot access this ticket");
        }

        List<ReplyDTO> replies = replyService.getReplies(id);
        List<TicketHistoryDTO> history = historyService.getHistory(id);

        ClassificationSuggestionDTO suggestion = null;
        if (currentUser.getRole() != UserRole.CUSTOMER) {
            suggestion = classificationService.classify(
                    ticket.getOrganization().getId(), ticket.getSubject(), ticket.getDescription(), ticket.getDomain());
        }

        Long remainingMins = slaService.calculateRemainingMinutes(ticket.getResolutionDeadline());

        return TicketDetailDTO.builder()
                .id(ticket.getId())
                .ticketNumber(ticket.getTicketNumber())
                .organizationId(ticket.getOrganization().getId())
                .customerId(ticket.getCustomer().getId())
                .customerName(ticket.getCustomer().getFullName())
                .customerEmail(ticket.getCustomer().getEmail())
                .subject(ticket.getSubject())
                .description(ticket.getDescription())
                .domain(ticket.getDomain())
                .categoryId(ticket.getCategory() != null ? ticket.getCategory().getId() : null)
                .categoryName(ticket.getCategory() != null ? ticket.getCategory().getName() : null)
                .priority(ticket.getPriority())
                .status(ticket.getStatus())
                .assignedTeamId(ticket.getAssignedTeam() != null ? ticket.getAssignedTeam().getId() : null)
                .assignedTeamName(ticket.getAssignedTeam() != null ? ticket.getAssignedTeam().getName() : null)
                .assignedAgentId(ticket.getAssignedAgent() != null ? ticket.getAssignedAgent().getId() : null)
                .assignedAgentName(ticket.getAssignedAgent() != null ? ticket.getAssignedAgent().getFullName() : null)
                .firstResponseDeadline(ticket.getFirstResponseDeadline())
                .resolutionDeadline(ticket.getResolutionDeadline())
                .firstResponseAt(ticket.getFirstResponseAt())
                .resolvedAt(ticket.getResolvedAt())
                .closedAt(ticket.getClosedAt())
                .slaFirstResponseBreached(ticket.getSlaFirstResponseBreached())
                .slaResolutionBreached(ticket.getSlaResolutionBreached())
                .slaRemainingMinutes(remainingMins)
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .replies(replies)
                .history(history)
                .classificationSuggestion(suggestion)
                .build();
    }

    @Transactional
    public TicketDTO updateTicketStatus(Long id, TicketStatus newStatus, Long userId, String comment) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + id));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        SecurityUtils.validateOrgAccess(ticket.getOrganization().getId());

        // Validate customer reopen
        if (user.getRole() == UserRole.CUSTOMER) {
            if (!ticket.getCustomer().getId().equals(user.getId())) {
                throw new UnauthorizedAccessException("Unauthorized");
            }
            if (newStatus == TicketStatus.OPEN && ticket.getStatus() == TicketStatus.RESOLVED) {
                // Allowed to reopen
            } else {
                throw new BadRequestException("Customers can only reopen resolved tickets");
            }
        }

        TicketStatus oldStatus = ticket.getStatus();
        ticket.setStatus(newStatus);

        if (newStatus == TicketStatus.RESOLVED && ticket.getResolvedAt() == null) {
            ticket.setResolvedAt(LocalDateTime.now());
            emailService.sendResolutionNotification(ticket);
        } else if (newStatus == TicketStatus.CLOSED && ticket.getClosedAt() == null) {
            ticket.setClosedAt(LocalDateTime.now());
        }

        ticket = ticketRepository.save(ticket);

        historyService.recordChange(ticket, user, "STATUS", oldStatus.name(), newStatus.name(),
                comment != null ? comment : "Status updated to " + newStatus.name());

        emailService.sendStatusUpdate(ticket, oldStatus.name(), newStatus.name());

        return mapToDTO(ticket);
    }

    @Transactional
    public TicketDTO assignTicket(Long id, Long agentId, Long teamId, Long actorUserId) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + id));

        User actor = userRepository.findById(actorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + actorUserId));

        SecurityUtils.validateOrgAccess(ticket.getOrganization().getId());

        if (teamId != null) {
            SupportTeam team = teamRepository.findById(teamId)
                    .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + teamId));
            String oldTeamName = ticket.getAssignedTeam() != null ? ticket.getAssignedTeam().getName() : "Unassigned";
            ticket.setAssignedTeam(team);
            historyService.recordChange(ticket, actor, "ASSIGNED_TEAM", oldTeamName, team.getName(), "Assigned team updated");
        }

        if (agentId != null) {
            User agent = userRepository.findById(agentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Agent not found: " + agentId));
            String oldAgentName = ticket.getAssignedAgent() != null ? ticket.getAssignedAgent().getFullName() : "Unassigned";
            ticket.setAssignedAgent(agent);
            historyService.recordChange(ticket, actor, "ASSIGNED_AGENT", oldAgentName, agent.getFullName(), "Assigned agent updated");
            emailService.sendTicketAssigned(ticket);
        }

        ticket = ticketRepository.save(ticket);
        return mapToDTO(ticket);
    }

    @Transactional
    public TicketDTO updateTicketDetails(Long id, TicketUpdateRequest request, Long actorUserId) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + id));

        User actor = userRepository.findById(actorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + actorUserId));

        SecurityUtils.validateOrgAccess(ticket.getOrganization().getId());

        if (request.getStatus() != null && request.getStatus() != ticket.getStatus()) {
            updateTicketStatus(id, request.getStatus(), actorUserId, request.getComment());
        }

        if (request.getPriority() != null && request.getPriority() != ticket.getPriority()) {
            Priority oldPriority = ticket.getPriority();
            ticket.setPriority(request.getPriority());
            slaService.applySlaPolicy(ticket);
            historyService.recordChange(ticket, actor, "PRIORITY", oldPriority.name(), request.getPriority().name(), "Priority changed");
        }

        if (request.getCategoryId() != null) {
            TicketCategory cat = categoryRepository.findById(request.getCategoryId()).orElse(null);
            ticket.setCategory(cat);
        }

        if (request.getAssignedTeamId() != null || request.getAssignedAgentId() != null) {
            assignTicket(id, request.getAssignedAgentId(), request.getAssignedTeamId(), actorUserId);
        }

        ticket = ticketRepository.save(ticket);
        return mapToDTO(ticket);
    }

    @Transactional(readOnly = true)
    public PageDTO<TicketDTO> searchTickets(TicketSearchRequest req, Long orgId, Long currentUserId) {
        SecurityUtils.validateOrgAccess(orgId);
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Ticket> list = ticketRepository.findAll().stream()
                .filter(t -> t.getOrganization().getId().equals(orgId))
                .toList();

        // If customer, only show customer's tickets
        if (currentUser.getRole() == UserRole.CUSTOMER) {
            list = list.stream().filter(t -> t.getCustomer().getId().equals(currentUser.getId())).toList();
        }

        // Filtering
        if (req.getStatus() != null) {
            list = list.stream().filter(t -> t.getStatus() == req.getStatus()).toList();
        }
        if (req.getPriority() != null) {
            list = list.stream().filter(t -> t.getPriority() == req.getPriority()).toList();
        }
        if (req.getDomain() != null && !req.getDomain().isBlank()) {
            list = list.stream().filter(t -> t.getDomain() != null && t.getDomain().equalsIgnoreCase(req.getDomain())).toList();
        }
        if (req.getCategoryId() != null) {
            list = list.stream().filter(t -> t.getCategory() != null && t.getCategory().getId().equals(req.getCategoryId())).toList();
        }
        if (req.getAssignedAgentId() != null) {
            list = list.stream().filter(t -> t.getAssignedAgent() != null && t.getAssignedAgent().getId().equals(req.getAssignedAgentId())).toList();
        }
        if (req.getAssignedTeamId() != null) {
            list = list.stream().filter(t -> t.getAssignedTeam() != null && t.getAssignedTeam().getId().equals(req.getAssignedTeamId())).toList();
        }
        if (req.getCustomerId() != null) {
            list = list.stream().filter(t -> t.getCustomer().getId().equals(req.getCustomerId())).toList();
        }
        if (Boolean.TRUE.equals(req.getOverdue())) {
            LocalDateTime now = LocalDateTime.now();
            list = list.stream().filter(t -> t.getStatus() != TicketStatus.RESOLVED && t.getStatus() != TicketStatus.CLOSED)
                    .filter(t -> (t.getResolutionDeadline() != null && now.isAfter(t.getResolutionDeadline()))
                            || Boolean.TRUE.equals(t.getSlaFirstResponseBreached())
                            || Boolean.TRUE.equals(t.getSlaResolutionBreached()))
                    .toList();
        }
        if (req.getQuery() != null && !req.getQuery().isBlank()) {
            String q = req.getQuery().toLowerCase();
            list = list.stream().filter(t ->
                    t.getTicketNumber().toLowerCase().contains(q)
                            || t.getSubject().toLowerCase().contains(q)
                            || t.getDescription().toLowerCase().contains(q)
                            || (t.getCustomer() != null && t.getCustomer().getFullName().toLowerCase().contains(q))
            ).toList();
        }

        // Sorting
        Comparator<Ticket> comparator = Comparator.comparing(Ticket::getCreatedAt);
        if ("priority".equalsIgnoreCase(req.getSortBy())) {
            comparator = Comparator.comparing(Ticket::getPriority);
        } else if ("status".equalsIgnoreCase(req.getSortBy())) {
            comparator = Comparator.comparing(Ticket::getStatus);
        } else if ("ticketNumber".equalsIgnoreCase(req.getSortBy())) {
            comparator = Comparator.comparing(Ticket::getTicketNumber);
        }

        if ("DESC".equalsIgnoreCase(req.getSortDirection())) {
            comparator = comparator.reversed();
        }

        list = list.stream().sorted(comparator).toList();

        // Pagination
        int total = list.size();
        int page = Math.max(0, req.getPage());
        int size = Math.max(1, req.getSize());
        int fromIndex = Math.min(page * size, total);
        int toIndex = Math.min(fromIndex + size, total);

        List<TicketDTO> pageContent = list.subList(fromIndex, toIndex).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        int totalPages = (int) Math.ceil((double) total / size);

        return PageDTO.<TicketDTO>builder()
                .content(pageContent)
                .page(page)
                .size(size)
                .totalElements(total)
                .totalPages(totalPages)
                .first(page == 0)
                .last(page >= totalPages - 1)
                .build();
    }

    private synchronized String generateTicketNumber() {
        int year = Year.now().getValue();
        long count = ticketRepository.count() + 1;
        return String.format("SD-%d-%05d", year, count);
    }

    public TicketDTO mapToDTO(Ticket ticket) {
        return TicketDTO.builder()
                .id(ticket.getId())
                .ticketNumber(ticket.getTicketNumber())
                .organizationId(ticket.getOrganization() != null ? ticket.getOrganization().getId() : null)
                .customerId(ticket.getCustomer() != null ? ticket.getCustomer().getId() : null)
                .customerName(ticket.getCustomer() != null ? ticket.getCustomer().getFullName() : null)
                .customerEmail(ticket.getCustomer() != null ? ticket.getCustomer().getEmail() : null)
                .subject(ticket.getSubject())
                .description(ticket.getDescription())
                .domain(ticket.getDomain())
                .categoryId(ticket.getCategory() != null ? ticket.getCategory().getId() : null)
                .categoryName(ticket.getCategory() != null ? ticket.getCategory().getName() : null)
                .priority(ticket.getPriority())
                .status(ticket.getStatus())
                .assignedTeamId(ticket.getAssignedTeam() != null ? ticket.getAssignedTeam().getId() : null)
                .assignedTeamName(ticket.getAssignedTeam() != null ? ticket.getAssignedTeam().getName() : null)
                .assignedAgentId(ticket.getAssignedAgent() != null ? ticket.getAssignedAgent().getId() : null)
                .assignedAgentName(ticket.getAssignedAgent() != null ? ticket.getAssignedAgent().getFullName() : null)
                .firstResponseDeadline(ticket.getFirstResponseDeadline())
                .resolutionDeadline(ticket.getResolutionDeadline())
                .firstResponseAt(ticket.getFirstResponseAt())
                .resolvedAt(ticket.getResolvedAt())
                .closedAt(ticket.getClosedAt())
                .slaFirstResponseBreached(ticket.getSlaFirstResponseBreached())
                .slaResolutionBreached(ticket.getSlaResolutionBreached())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }
}

